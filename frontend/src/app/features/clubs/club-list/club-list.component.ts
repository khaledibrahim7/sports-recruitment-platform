import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ClubService } from '../../../core/services/club.service';
import { ClubProfile } from '../../../core/models/models';

@Component({
  selector: 'app-club-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './club-list.component.html',
  styleUrls: ['./club-list.component.scss']
})
export class ClubListComponent implements OnInit {
  public clubs: ClubProfile[] = [];
  public isLoading = false;

  constructor(private clubService: ClubService) {}

  ngOnInit(): void {
    this.loadClubs();
  }

  public loadClubs(): void {
    this.isLoading = true;
    this.clubService.getClubs().subscribe({
      next: (res) => {
        this.isLoading = false;
        if (res.success && res.data) {
          this.clubs = res.data;
        }
      },
      error: () => {
        this.isLoading = false;
      }
    });
  }
}
