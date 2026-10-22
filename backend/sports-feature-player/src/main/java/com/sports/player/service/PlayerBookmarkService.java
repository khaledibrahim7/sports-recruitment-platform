package com.sports.player.service;

import com.sports.player.dto.PublicPlayerProfileDto;
import java.util.List;

public interface PlayerBookmarkService {
    boolean toggleBookmark(Long scoutUserId, Long playerId);
    boolean isBookmarked(Long scoutUserId, Long playerId);
    List<PublicPlayerProfileDto> getBookmarkedPlayers(Long scoutUserId);
    List<Long> getBookmarkedPlayerIds(Long scoutUserId);
}
