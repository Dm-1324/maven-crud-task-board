package com.example.voting.controller;

import com.example.voting.model.Poll;
import com.example.voting.service.PollService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/polls")
@CrossOrigin
public class PollController {
    private final PollService service;

    public PollController(PollService service) { this.service = service; }

    @GetMapping
    public List<Poll> getAll() { return service.getAll(); }

    @GetMapping("/{id}")
    public Poll getOne(@PathVariable Long id) { return service.getById(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Poll create(@RequestBody CreatePollRequest request) {
        return service.createPoll(request.question(), request.options());
    }

    @PostMapping("/{id}/vote")
    public Poll vote(@PathVariable Long id, @RequestBody VoteRequest request) {
        return service.vote(id, request.voterName(), request.optionId());
    }

    @PostMapping("/{id}/close")
    public Poll close(@PathVariable Long id) { return service.closePoll(id); }

    public record CreatePollRequest(String question, List<String> options) {}
    public record VoteRequest(String voterName, Long optionId) {}
}
