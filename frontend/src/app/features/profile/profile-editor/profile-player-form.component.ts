import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PlayerPosition, PlayerPositionLabels, handleImageFallback } from '../../../core/models/models';
import { RadarChartComponent } from '../../../shared/components/radar-chart/radar-chart.component';

export interface PlayerFormModel {
  fullName: string;
  age: number;
  birthDate: string;
  nationality: string;
  cityCountry: string;
  currentTeam: string;
  competitionLevel: string;
  playerGoal: string;
  availableForTransfer: boolean;
  position: PlayerPosition;
  secondaryPositions: PlayerPosition[];
  videoUrl: string;
  videoUrls: string[];
  uploadedVideoUrls: string[];
  removedUploadedVideoUrls: string[];
  phone: string;
  bio: string;
  avatarUrl: string;
  heightCm: number;
  weightKg: number;
  preferredFoot: 'RIGHT' | 'LEFT' | 'BOTH';
  pace: number;
  shooting: number;
  passing: number;
  dribbling: number;
  defending: number;
  physicality: number;
}

@Component({
  selector: 'app-profile-player-form',
  standalone: true,
  imports: [CommonModule, FormsModule, RadarChartComponent],
  templateUrl: './profile-player-form.component.html',
  styleUrls: ['./profile-editor.component.scss']
})
export class ProfilePlayerFormComponent {
  @Input({ required: true }) form!: PlayerFormModel;
  @Input() newVideoUrl = '';
  @Input() isUploadingImage = false;
  @Input() isUploadingVideo = false;
  @Input() mediaUrlFn: (url?: string | null) => string = (url) => url || 'default-avatar.png';

  @Output() newVideoUrlChange = new EventEmitter<string>();
  @Output() avatarUpload = new EventEmitter<Event>();
  @Output() videoFileUpload = new EventEmitter<Event>();
  @Output() addVideo = new EventEmitter<void>();
  @Output() removeVideo = new EventEmitter<number>();

  public readonly playerPositions = Object.keys(PlayerPositionLabels) as PlayerPosition[];
  public readonly playerPositionLabels = PlayerPositionLabels;
  public readonly onAvatarError = handleImageFallback;

  public toggleSecondaryPosition(pos: PlayerPosition): void {
    if (pos === this.form.position) return;
    const idx = this.form.secondaryPositions.indexOf(pos);
    if (idx >= 0) {
      this.form.secondaryPositions.splice(idx, 1);
    } else {
      this.form.secondaryPositions.push(pos);
    }
  }

  public isSecondaryPositionSelected(pos: PlayerPosition): boolean {
    return this.form.secondaryPositions.includes(pos);
  }
}
