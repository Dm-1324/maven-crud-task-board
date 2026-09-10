package com.example.voting.service;

import com.example.voting.exception.AlreadyVotedException;
import com.example.voting.exception.PollClosedException;
import com.example.voting.model.Poll;
import com.example.voting.model.PollOption;
import com.example.voting.model.Vote;
import com.example.voting.repository.PollOptionRepository;
import com.example.voting.repository.PollRepository;
import com.example.voting.repository.VoteRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PollService {
    private final PollRepository pollRepository;
    private final PollOptionRepository optionRepository;
    private final VoteRepository voteRepository;

    public PollService(PollRepository pollRepository, PollOptionRepository optionRepository, VoteRepository voteRepository) {
        this.pollRepository = pollRepository;
        this.optionRepository = optionRepository;
        this.voteRepository = voteRepository;
    }

    public List<Poll> getAll() { return pollRepository.findAll(); }

    public Poll getById(Long id) {
        return pollRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Poll not found: " + id));
    }

    @Transactional
    public Poll createPoll(String question, List<String> optionTexts) {
        if (question == null || question.isBlank()) throw new IllegalArgumentException("Question is required");
        if (optionTexts == null || optionTexts.size() < 2) throw new IllegalArgumentException("A poll needs at least 2 options");
        if (optionTexts.stream().anyMatch(text -> text == null || text.isBlank())) {
            throw new IllegalArgumentException("Options cannot be blank");
        }

        Poll poll = new Poll(question.trim());
        for (String text : optionTexts) {
            PollOption option = new PollOption(text.trim());
            option.setPoll(poll);
            poll.getOptions().add(option);
        }
        return pollRepository.save(poll);
    }

    @Transactional
    public Poll vote(Long pollId, String voterName, Long optionId) {
        Poll poll = getById(pollId);
        if (!poll.isOpen()) throw new PollClosedException("This poll is closed");
        if (voterName == null || voterName.isBlank()) throw new IllegalArgumentException("Voter name is required");
        if (optionId == null) throw new IllegalArgumentException("Option is required");

        String normalizedName = voterName.trim();
        voteRepository.findByPollIdAndVoterName(pollId, normalizedName)
                .ifPresent(v -> { throw new AlreadyVotedException(normalizedName + " has already voted on this poll"); });

        PollOption option = poll.getOptions().stream()
                .filter(o -> optionId.equals(o.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid option for this poll: " + optionId));

        option.setVoteCount(option.getVoteCount() + 1);
        optionRepository.save(option);
        try {
            voteRepository.saveAndFlush(new Vote(pollId, normalizedName, optionId));
        } catch (DataIntegrityViolationException e) {
            throw new AlreadyVotedException(normalizedName + " has already voted on this poll");
        }
        return poll;
    }

    @Transactional
    public Poll closePoll(Long id) {
        Poll poll = getById(id);
        poll.setOpen(false);
        return pollRepository.save(poll);
    }
}
