export type Role = 'PLAYER' | 'ACADEMY' | 'CLUB';

export type PlayerPosition = 
  | 'GOALKEEPER'
  | 'CENTER_BACK'
  | 'LEFT_BACK'
  | 'RIGHT_BACK'
  | 'DEFENSIVE_MIDFIELDER'
  | 'CENTRAL_MIDFIELDER'
  | 'ATTACKING_MIDFIELDER'
  | 'LEFT_WINGER'
  | 'RIGHT_WINGER'
  | 'STRIKER';

export const PlayerPositionLabels: Record<PlayerPosition, { ar: string; en: string }> = {
  GOALKEEPER: { ar: 'حارس مرمى', en: 'Goalkeeper' },
  CENTER_BACK: { ar: 'قلب دفاع', en: 'Center Back' },
  LEFT_BACK: { ar: 'ظهير أيسر', en: 'Left Back' },
  RIGHT_BACK: { ar: 'ظهير أيمن', en: 'Right Back' },
  DEFENSIVE_MIDFIELDER: { ar: 'وسط مدافع (ارتكاز)', en: 'Defensive Midfielder' },
  CENTRAL_MIDFIELDER: { ar: 'وسط ملعب', en: 'Central Midfielder' },
  ATTACKING_MIDFIELDER: { ar: 'صانع ألعاب', en: 'Attacking Midfielder' },
  LEFT_WINGER: { ar: 'جناح أيسر', en: 'Left Winger' },
  RIGHT_WINGER: { ar: 'جناح أيمن', en: 'Right Winger' },
  STRIKER: { ar: 'مهاجم صريح', en: 'Striker' }
};

export interface User {
  userId: number;
  fullName: string;
  email: string;
  role: Role;
}

export interface AuthResponse {
  accessToken: string;
  token?: string;
  userId: number;
  fullName: string;
  email: string;
  role: Role;
  profile?: PlayerProfile | any;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

export interface PlayerProfile {
  id: number;
  userId: number;
  fullName: string;
  email: string;
  age: number;
  birthDate?: string;
  nationality?: string;
  cityCountry?: string;
  currentTeam?: string;
  competitionLevel?: string;
  playerGoal?: string;
  availableForTransfer?: boolean;
  position: PlayerPosition;
  secondaryPositions?: PlayerPosition[];
  videoUrl?: string;
  videoUrls?: string[];
  uploadedVideoUrls?: string[];
  phone?: string;
  bio?: string;
  avatarUrl?: string;

  // Physical Specs & Radar Skills
  heightCm?: number;
  weightKg?: number;
  preferredFoot?: 'RIGHT' | 'LEFT' | 'BOTH';
  pace?: number;
  shooting?: number;
  passing?: number;
  dribbling?: number;
  defending?: number;
  physicality?: number;
}

export type PublicPlayerProfile = Omit<PlayerProfile, 'userId' | 'email' | 'birthDate' | 'nationality' | 'phone' | 'uploadedVideoUrls' | 'availableForTransfer'>;

export interface PlayerSearchPage {
  items: PublicPlayerProfile[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface AcademyProfile {
  id: number;
  userId: number;
  academyName: string;
  email: string;
  websiteOrPageUrl?: string;
  cityCountry?: string;
  contactPhone?: string;
  logoUrl?: string;
  description?: string;
  ageGroups?: string[];
  trainingType?: string;
}

export interface ClubProfile {
  id: number;
  userId: number;
  clubName: string;
  email: string;
  logoUrl?: string;
  cityCountry?: string;
  contactPhone?: string;
  websiteOrPageUrl?: string;
  description?: string;
  leagueLevel?: string;
  activelyRecruiting?: boolean;
}

export interface TrialOffer {
  id: number;
  scoutUserId: number;
  scoutName: string;
  scoutRole: string;
  playerId: number;
  playerName: string;
  trialDate: string;
  location: string;
  details?: string;
  status: 'PENDING' | 'ACCEPTED' | 'REJECTED';
  createdAt: string;
}

export interface NotificationItem {
  id: number;
  recipientUserId: number;
  title: string;
  message: string;
  type: string;
  isRead: boolean;
  createdAt: string;
}
