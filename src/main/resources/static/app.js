const API = '/api/tasks';
const STATUSES = ['TODO', 'IN_PROGRESS', 'DONE'];
const message = document.getElementById('message');

function showMessage(text) {
  message.textContent = text || '';
  if (text) setTimeout(() => { message.textContent = ''; }, 3000);
}

async function request(url, options = {}) {
  const response = await fetch(url, options);
  let data = null;
  try { data = await response.json(); } catch (_) { /* empty response */ }
  if (!response.ok) throw new Error(data?.error || `Request failed (${response.status})`);
  return data;
}

async function load() {
  try {
    const tasks = await request(API);
    STATUSES.forEach(status => {
      document.getElementById(`col-${status}`).replaceChildren();
    });
    tasks.forEach(renderCard);
  } catch (error) {
    showMessage(error.message);
  }
}

function renderCard(task) {
  const card = document.createElement('article');
  card.className = 'card';
  card.draggable = true;
  card.dataset.id = task.id;

  const deleteButton = document.createElement('button');
  deleteButton.className = 'del';
  deleteButton.type = 'button';
  deleteButton.setAttribute('aria-label', `Delete task ${task.title}`);
  deleteButton.textContent = '✕';
  deleteButton.addEventListener('click', () => deleteTask(task.id));

  const title = document.createElement('div');
  title.className = 'card-title';
  title.textContent = task.title;

  const description = document.createElement('div');
  description.className = 'card-description';
  description.textContent = task.description || '';

  const id = document.createElement('small');
  id.textContent = `#${task.id}`;

  card.append(deleteButton, title, description, id);
  card.addEventListener('dragstart', event => event.dataTransfer.setData('text/plain', String(task.id)));
  document.getElementById(`col-${task.status}`).appendChild(card);
}

document.querySelectorAll('.column').forEach(column => {
  column.addEventListener('dragover', event => { event.preventDefault(); column.classList.add('dragover'); });
  column.addEventListener('dragleave', () => column.classList.remove('dragover'));
  column.addEventListener('drop', async event => {
    event.preventDefault();
    column.classList.remove('dragover');
    const id = event.dataTransfer.getData('text/plain');
    if (!id) return;
    try {
      await request(`${API}/${encodeURIComponent(id)}/status`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ status: column.dataset.status })
      });
      await load();
    } catch (error) { showMessage(error.message); }
  });
});

document.getElementById('addForm').addEventListener('submit', async event => {
  event.preventDefault();
  const title = document.getElementById('title').value.trim();
  const description = document.getElementById('desc').value.trim();
  if (!title) return;
  try {
    await request(API, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ title, description, status: 'TODO' })
    });
    event.target.reset();
    await load();
  } catch (error) { showMessage(error.message); }
});

async function deleteTask(id) {
  try {
    await request(`${API}/${encodeURIComponent(id)}`, { method: 'DELETE' });
    await load();
  } catch (error) { showMessage(error.message); }
}

load();
