package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.repository.AdminCardFilter;
import com.example.bankcards.service.CardService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/card/v1")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    @GetMapping("/all")
    public PagedModel<Card> getAll(@ModelAttribute AdminCardFilter filter, Pageable pageable) {
        Page<Card> cards = cardService.getAll(filter, pageable);
        return new PagedModel<>(cards);
    }

    @GetMapping("/myCards")
    public PagedModel<Card> getAllMyCards(@ModelAttribute CardFilter filter, Pageable pageable) {
        Page<Card> cards = cardService.getAllMyCards(filter, pageable);
        return new PagedModel<>(cards);
    }

    @GetMapping("/{id}")
    public CardDto getOne(@PathVariable Long id) {
        return cardService.getOne(id);
    }

    @GetMapping("/by-ids")
    public List<CardDto> getMany(@RequestParam List<Long> ids) {
        return cardService.getMany(ids);
    }

    @PostMapping
    public CardDto create(@RequestBody CardDto dto) {
        return cardService.create(dto);
    }

    @PatchMapping("changeCardStatus/{id}")
    public CardDto patch(@PathVariable Long id, @RequestBody JsonNode patchNode) throws IOException {
        return cardService.patch(id, patchNode);
    }

    @PatchMapping("changeManyCardStatus")
    public List<Long> patchMany(@RequestParam List<Long> ids, @RequestBody JsonNode patchNode) throws IOException {
        return cardService.patchMany(ids, patchNode);
    }

    @DeleteMapping("/{id}")
    public CardDto delete(@PathVariable Long id) {
        return cardService.delete(id);
    }

    @DeleteMapping
    public void deleteMany(@RequestParam List<Long> ids) {
        cardService.deleteMany(ids);
    }
}
