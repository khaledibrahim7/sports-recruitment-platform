import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PlayerPositionLabels, PublicPlayerProfile, handleImageFallback } from '../../../core/models/models';

@Component({
  selector: 'app-profile-watchlist-tab',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule],
  template: `
    <div class="mt-4">
      <h3 class="mb-3 text-gold"><i class="fa-solid fa-bookmark"></i> قائمة اللاعبين المحفوظين للمتابعة</h3>

      @if (bookmarkedPlayers.length === 0) {
        <div class="text-center py-5 text-muted glass-card">
          <i class="fa-solid fa-bookmark fa-3x mb-2 opacity-50"></i>
          <p>قائمة المتابعة فارغة حالياً. اضغط على أيقونة الإشارة المرجعية <i class="fa-solid fa-bookmark text-gold"></i> بكروت سوق اللاعبين لحفظهم هنا.</p>
        </div>
      } @else {
        <div class="players-grid">
          @for (player of bookmarkedPlayers; track player.id) {
            <div class="glass-card player-card p-3 d-flex flex-column align-items-center text-center">
              <div class="player-avatar mb-2">
                <img
                  [src]="mediaUrlFn(player.avatarUrl)"
                  (error)="onAvatarError($event)"
                  [alt]="player.fullName"
                  class="player-avatar-img" loading="lazy" decoding="async"
                />
              </div>
              <h4 class="m-0 text-white">{{ player.fullName }}</h4>
              <span class="badge badge-player mt-1 mb-2">{{ playerPositionLabels[player.position].ar || player.position }}</span>
              <button type="button" (click)="remove.emit(player)" class="btn btn-outline-danger btn-xs w-100 mt-2">
                <i class="fa-solid fa-trash-can"></i> إزالة من القائمة
              </button>
            </div>
          }
        </div>
      }
    </div>
  `
})
export class ProfileWatchlistTabComponent {
  @Input() bookmarkedPlayers: PublicPlayerProfile[] = [];
  @Input() mediaUrlFn: (url?: string | null) => string = (url) => url || 'default-avatar.png';
  @Output() remove = new EventEmitter<PublicPlayerProfile>();

  public readonly playerPositionLabels = PlayerPositionLabels;
  public readonly onAvatarError = handleImageFallback;
}
