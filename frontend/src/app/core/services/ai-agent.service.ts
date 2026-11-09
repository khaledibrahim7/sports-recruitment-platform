import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse } from '../models/models';

export interface AiChatRequest {
  message: string;
  context?: string;
}

export interface AiChatResponse {
  reply: string;
  success: boolean;
}

@Injectable({ providedIn: 'root' })
export class AiAgentService {
  private readonly http = inject(HttpClient);
  private readonly base = '/api/v1/ai-agent';

  chat(request: AiChatRequest): Observable<ApiResponse<AiChatResponse>> {
    return this.http.post<ApiResponse<AiChatResponse>>(`${this.base}/chat`, request);
  }

  analyzePlayer(playerId: number): Observable<ApiResponse<AiChatResponse>> {
    return this.http.post<ApiResponse<AiChatResponse>>(
      `${this.base}/analyze-player/${playerId}`, {}
    );
  }

  recommend(request: AiChatRequest): Observable<ApiResponse<AiChatResponse>> {
    return this.http.post<ApiResponse<AiChatResponse>>(`${this.base}/recommend`, request);
  }
}
