import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse, TrialOffer } from '../models/models';

@Injectable({
  providedIn: 'root'
})
export class TrialOfferService {
  private readonly API_URL = '/api/v1/trial-offers';

  constructor(private http: HttpClient) {}

  public sendOffer(playerId: number, trialDate: string, location: string, details: string): Observable<ApiResponse<TrialOffer>> {
    return this.http.post<ApiResponse<TrialOffer>>(this.API_URL, {
      playerId,
      trialDate,
      location,
      details
    });
  }

  public getMyOffers(): Observable<ApiResponse<TrialOffer[]>> {
    return this.http.get<ApiResponse<TrialOffer[]>>(`${this.API_URL}/my-offers`);
  }

  public getSentOffers(): Observable<ApiResponse<TrialOffer[]>> {
    return this.http.get<ApiResponse<TrialOffer[]>>(`${this.API_URL}/sent-offers`);
  }

  public updateStatus(offerId: number, status: 'ACCEPTED' | 'REJECTED'): Observable<ApiResponse<TrialOffer>> {
    const params = new HttpParams().set('status', status);
    return this.http.patch<ApiResponse<TrialOffer>>(`${this.API_URL}/${offerId}/status`, {}, { params });
  }
}
