const API = '/api/polls';
const pollsContainer = document.getElementById('polls');
const message = document.getElementById('message');
let voterName = localStorage.getItem('voterName') || '';

function showMessage(text) {
  message.textContent = text || '';
  if (text) setTimeout(() => { message.textContent = ''; }, 3000);
}

async function request(url, options = {}) {
  const response = await fetch(url, options);
  let data = null;
  try { data = await response.json(); } catch (_) {}
  if (!response.ok) throw new Error(data?.error || `Request failed (${response.status})`);
  return data;
}

async function loadPolls() {
  try {
    const polls = await request(API);
    pollsContainer.replaceChildren(...polls.map(renderPoll));
  } catch (error) {
    showMessage(error.message);
  }
}

function renderPoll(poll) {
  const card = document.createElement('article');
  card.className = 'poll';

  const heading = document.createElement('h2');
  heading.textContent = poll.question;
  if (!poll.open) {
    const tag = document.createElement('span');
    tag.className = 'closed-tag';
    tag.textContent = 'CLOSED';
    heading.appendChild(tag);
  }
  card.appendChild(heading);

  if (poll.open) {
    const input = document.createElement('input');
    input.className = 'voter-input';
    input.id = `voter-${poll.id}`;
    input.placeholder = 'Your name';
    input.maxLength = 100;
    input.value = voterName;
    card.appendChild(input);
  }

  const total = poll.options.reduce((sum, option) => sum + option.voteCount, 0);
  const error = document.createElement('div');
  error.className = 'error';
  error.id = `error-${poll.id}`;

  poll.options.forEach(option => {
    const row = document.createElement('div');
    row.className = 'option-row';

    const header = document.createElement('div');
    header.className = 'option-header';
    const label = document.createElement('span');
    label.textContent = option.text;
    const count = document.createElement('span');
    count.textContent = `${option.voteCount} vote${option.voteCount === 1 ? '' : 's'}`;
    header.append(label, count);

    const bar = document.createElement('div');
    bar.className = 'bar-bg';
    const fill = document.createElement('div');
    fill.className = 'bar-fill';
    fill.style.width = `${total ? Math.round((option.voteCount / total) * 100) : 0}%`;
    bar.appendChild(fill);

    row.append(header, bar);

    if (poll.open) {
      const button = document.createElement('button');
      button.type = 'button';
      button.textContent = 'Vote';
      button.addEventListener('click', () => vote(poll.id, option.id));
      row.appendChild(button);
    }
    card.appendChild(row);
  });

  if (poll.open) {
    const actions = document.createElement('div');
    actions.className = 'actions';
    const closeButton = document.createElement('button');
    closeButton.type = 'button';
    closeButton.textContent = 'Close Poll';
    closeButton.addEventListener('click', () => closePoll(poll.id));
    actions.appendChild(closeButton);
    card.appendChild(actions);
  }

  card.appendChild(error);
  return card;
}

async function vote(pollId, optionId) {
  const input = document.getElementById(`voter-${pollId}`);
  voterName = input.value.trim();
  if (!voterName) {
    document.getElementById(`error-${pollId}`).textContent = 'Enter your name first';
    return;
  }
  localStorage.setItem('voterName', voterName);

  try {
    await request(`${API}/${pollId}/vote`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ voterName, optionId })
    });
    await loadPolls();
  } catch (error) {
    document.getElementById(`error-${pollId}`).textContent = error.message;
  }
}

async function closePoll(pollId) {
  try {
    await request(`${API}/${pollId}/close`, { method: 'POST' });
    await loadPolls();
  } catch (error) {
    document.getElementById(`error-${pollId}`).textContent = error.message;
  }
}

document.getElementById('createForm').addEventListener('submit', async event => {
  event.preventDefault();
  const question = document.getElementById('question').value.trim();
  const opt1 = document.getElementById('opt1').value.trim();
  const opt2 = document.getElementById('opt2').value.trim();
  try {
    await request(API, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ question, options: [opt1, opt2] })
    });
    event.target.reset();
    await loadPolls();
  } catch (error) {
    showMessage(error.message);
  }
});

loadPolls();
setInterval(loadPolls, 3000);
