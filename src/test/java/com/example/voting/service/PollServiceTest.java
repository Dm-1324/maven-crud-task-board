package com.example.voting.service;

import com.example.voting.exception.AlreadyVotedException;
import com.example.voting.exception.PollClosedException;
import com.example.voting.model.Poll;
import com.example.voting.model.PollOption;
import com.example.voting.model.Vote;
import com.example.voting.repository.PollOptionRepository;
import com.example.voting.repository.PollRepository;
import com.example.voting.repository.VoteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PollServiceTest {
    @Mock private PollRepository pollRepository;
    @Mock private PollOptionRepository optionRepository;
    @Mock private VoteRepository voteRepository;
    @InjectMocks private PollService service;

    @Test void createPollWithBlankQuestionThrows() {
        assertThrows(IllegalArgumentException.class, () -> service.createPoll("", List.of("A", "B")));
    }

    @Test void createPollWithFewerThanTwoOptionsThrows() {
        assertThrows(IllegalArgumentException.class, () -> service.createPoll("Favorite color?", List.of("Red")));
    }

    @Test void createPollWithBlankOptionThrows() {
        assertThrows(IllegalArgumentException.class, () -> service.createPoll("Favorite color?", List.of("Red", "")));
    }

    @Test void createPollSavesSuccessfully() {
        when(pollRepository.save(any(Poll.class))).thenAnswer(inv -> inv.getArgument(0));
        Poll poll = service.createPoll("Favorite color?", List.of("Red", "Blue"));
        assertEquals("Favorite color?", poll.getQuestion());
        assertEquals(2, poll.getOptions().size());
        assertSame(poll, poll.getOptions().get(0).getPoll());
    }

    @Test void voteOnClosedPollThrows() {
        Poll poll = new Poll("Q?");
        poll.setOpen(false);
        when(pollRepository.findById(1L)).thenReturn(Optional.of(poll));
        assertThrows(PollClosedException.class, () -> service.vote(1L, "Dhruv", 1L));
    }

    @Test void voteTwiceBySameVoterThrows() {
        Poll poll = pollWithOption(1L);
        when(pollRepository.findById(1L)).thenReturn(Optional.of(poll));
        when(voteRepository.findByPollIdAndVoterName(1L, "Dhruv"))
                .thenReturn(Optional.of(new Vote(1L, "Dhruv", 1L)));
        assertThrows(AlreadyVotedException.class, () -> service.vote(1L, "Dhruv", 1L));
    }

    @Test void voteWithInvalidOptionThrows() {
        Poll poll = pollWithOption(1L);
        when(pollRepository.findById(1L)).thenReturn(Optional.of(poll));
        when(voteRepository.findByPollIdAndVoterName(1L, "Dhruv")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.vote(1L, "Dhruv", 999L));
    }

    @Test void voteIncrementsCountCorrectly() {
        Poll poll = pollWithOption(1L);
        when(pollRepository.findById(1L)).thenReturn(Optional.of(poll));
        when(voteRepository.findByPollIdAndVoterName(1L, "Dhruv")).thenReturn(Optional.empty());
        when(voteRepository.saveAndFlush(any(Vote.class))).thenAnswer(inv -> inv.getArgument(0));

        Poll result = service.vote(1L, "Dhruv", 1L);
        assertEquals(1, result.getOptions().get(0).getVoteCount());
        verify(optionRepository).save(any(PollOption.class));
        verify(voteRepository).saveAndFlush(any(Vote.class));
    }

    @Test void closePollSetsOpenFalse() {
        Poll poll = new Poll("Q?");
        when(pollRepository.findById(5L)).thenReturn(Optional.of(poll));
        when(pollRepository.save(any(Poll.class))).thenAnswer(inv -> inv.getArgument(0));
        assertFalse(service.closePoll(5L).isOpen());
    }

    private Poll pollWithOption(Long optionId) {
        Poll poll = new Poll("Q?");
        PollOption option = new PollOption("Red");
        try {
            var field = PollOption.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(option, optionId);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
        option.setPoll(poll);
        poll.getOptions().add(option);
        return poll;
    }
}
