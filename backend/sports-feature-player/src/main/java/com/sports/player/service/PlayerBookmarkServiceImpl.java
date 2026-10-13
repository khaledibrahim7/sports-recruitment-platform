package com.sports.player.service;

import com.sports.player.domain.PlayerBookmark;
import com.sports.player.domain.PlayerProfile;
import com.sports.player.dto.PlayerProfileDto;
import com.sports.player.repository.PlayerBookmarkRepository;
import com.sports.player.repository.PlayerProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PlayerBookmarkServiceImpl implements PlayerBookmarkService {

    private final PlayerBookmarkRepository bookmarkRepository;
    private final PlayerProfileRepository playerProfileRepository;

    public PlayerBookmarkServiceImpl(
            PlayerBookmarkRepository bookmarkRepository,
            PlayerProfileRepository playerProfileRepository) {
        this.bookmarkRepository = bookmarkRepository;
        this.playerProfileRepository = playerProfileRepository;
    }

    @Override
    @Transactional
    public boolean toggleBookmark(Long scoutUserId, Long playerId) {
        Optional<PlayerBookmark> existing = bookmarkRepository.findByScoutUserIdAndPlayerId(scoutUserId, playerId);
        if (existing.isPresent()) {
            bookmarkRepository.delete(existing.get());
            return false; // removed from bookmarks
        } else {
            PlayerProfile player = playerProfileRepository.findById(playerId)
                    .orElseThrow(() -> new IllegalArgumentException("Player profile not found with id: " + playerId));
            PlayerBookmark bookmark = new PlayerBookmark(scoutUserId, player);
            bookmarkRepository.save(bookmark);
            return true; // added to bookmarks
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isBookmarked(Long scoutUserId, Long playerId) {
        return bookmarkRepository.existsByScoutUserIdAndPlayerId(scoutUserId, playerId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlayerProfileDto> getBookmarkedPlayers(Long scoutUserId) {
        List<PlayerBookmark> bookmarks = bookmarkRepository.findAllByScoutUserIdWithPlayer(scoutUserId);
        return bookmarks.stream()
                .map(b -> PlayerProfileDto.fromEntity(b.getPlayer()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getBookmarkedPlayerIds(Long scoutUserId) {
        return bookmarkRepository.findBookmarkedPlayerIdsByScoutUserId(scoutUserId);
    }
}
