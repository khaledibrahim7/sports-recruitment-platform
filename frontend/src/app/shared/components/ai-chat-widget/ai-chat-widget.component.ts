import { Component, signal, ElementRef, ViewChild, AfterViewChecked, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AiAgentService } from '../../../core/services/ai-agent.service';

interface ChatMessage {
  role: 'user' | 'agent';
  text: string;
  timestamp: Date;
  loading?: boolean;
}

@Component({
  selector: 'app-ai-chat-widget',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <!-- Floating Toggle Button -->
    <button class="ai-fab" (click)="toggleChat()" [class.active]="isOpen()">
      @if (!isOpen()) {
        @if (!avatarError()) {
          <img src="images/kashif-avatar.png"
               (error)="avatarError.set(true)"
               class="fab-avatar" alt="كاشف">
        } @else {
          <span class="fab-avatar-fallback"><i class="fa-solid fa-robot"></i></span>
        }
        <span class="fab-label">كاشف AI</span>
      } @else {
        <i class="fa-solid fa-xmark"></i>
        <span class="fab-close-label">إغلاق</span>
      }
      @if (unread() > 0 && !isOpen()) {
        <span class="unread-dot">{{ unread() }}</span>
      }
    </button>

    <!-- Chat Panel -->
    @if (isOpen()) {
      <div class="ai-chat-panel" [class.open]="isOpen()">

        <!-- Header -->
        <div class="chat-header">
          <div class="agent-info">
            <div class="agent-avatar">
              @if (!avatarError()) {
                <img src="images/kashif-avatar.png"
                     (error)="avatarError.set(true)"
                     class="avatar-img" alt="كاشف">
              } @else {
                <i class="fa-solid fa-robot"></i>
              }
              <span class="status-dot"></span>
            </div>
            <div>
              <div class="agent-name">كاشف</div>
              <div class="agent-title">مساعد التكشيف الرياضي الذكي</div>
            </div>
          </div>
          <!-- New Chat only — no extra close button -->
          <button class="new-chat-btn" (click)="newChat()" title="محادثة جديدة">
            <i class="fa-solid fa-rotate-right"></i>
            <span>جديد</span>
          </button>
        </div>

        <!-- Suggestion chips (shown when no messages) -->
        @if (messages().length === 0) {
          <div class="suggestions-panel">
            <div class="greeting">
              <i class="fa-solid fa-hand-wave"></i>
              مرحباً! أنا <strong>كاشف</strong> — مساعدك الذكي للتكشيف الرياضي.
            </div>
            <div class="chips-label">اقتراحات سريعة:</div>
            <div class="chips">
              @for (chip of suggestionChips; track $index) {
                <button class="chip" (click)="sendChip(chip)">{{ chip }}</button>
              }
            </div>
          </div>
        }

        <!-- Messages -->
        <div class="messages-area" #messagesArea>
          @for (msg of messages(); track $index) {
            <div class="message" [class.user]="msg.role === 'user'" [class.agent]="msg.role === 'agent'">
              @if (msg.role === 'agent') {
                <div class="msg-avatar">
                  @if (!avatarError()) {
                    <img src="images/kashif-avatar.png"
                         (error)="avatarError.set(true)"
                         class="avatar-img-sm" alt="كاشف">
                  } @else {
                    <i class="fa-solid fa-robot"></i>
                  }
                </div>
              }
              <div class="msg-bubble">
                @if (msg.loading) {
                  <div class="typing-dots">
                    <span></span><span></span><span></span>
                  </div>
                } @else {
                  <span [innerHTML]="formatText(msg.text)"></span>
                }
                <div class="msg-time">{{ msg.timestamp | date:'HH:mm' }}</div>
              </div>
            </div>
          }
        </div>

        <!-- Input Area -->
        <div class="chat-input-area">
          <input
            type="text"
            [(ngModel)]="userInput"
            (keyup.enter)="sendMessage()"
            placeholder="اكتب سؤالك هنا..."
            [disabled]="isLoading()"
            class="chat-input"
            dir="rtl"
          />
          <button class="send-btn" (click)="sendMessage()" [disabled]="!userInput.trim() || isLoading()">
            @if (isLoading()) {
              <i class="fa-solid fa-spinner fa-spin"></i>
            } @else {
              <i class="fa-solid fa-paper-plane"></i>
              <span class="send-label">إرسال</span>
            }
          </button>
        </div>

      </div>
    }
  `,
  styles: [`
    :host { position: fixed; bottom: 2rem; left: 2rem; z-index: 9000; font-family: 'Cairo', sans-serif; }

    /* ── FAB Button ── */
    .ai-fab {
      display: flex; align-items: center; gap: 0.5rem;
      background: linear-gradient(135deg, #00ff87, #00c8ff);
      color: #0a0f1e; border: none; border-radius: 50px;
      padding: 0.5rem 1rem 0.5rem 0.5rem;
      font-size: 0.85rem; font-weight: 800;
      cursor: pointer; box-shadow: 0 4px 24px rgba(0,255,135,0.45);
      transition: all 0.25s ease; white-space: nowrap; position: relative;
      &:hover { transform: translateY(-2px); box-shadow: 0 8px 32px rgba(0,255,135,0.6); }
      &.active { padding: 0.5rem 1rem 0.5rem 0.75rem; }
    }
    .fab-avatar {
      width: 34px; height: 34px; border-radius: 50%;
      object-fit: cover; border: 2px solid rgba(10,15,30,0.3);
      flex-shrink: 0;
    }
    .fab-avatar-fallback {
      width: 34px; height: 34px; border-radius: 50%;
      background: rgba(10,15,30,0.2);
      display: flex; align-items: center; justify-content: center;
      font-size: 1rem; flex-shrink: 0;
    }
    .fab-label { font-weight: 800; font-size: 0.85rem; }
    .fab-close-label { font-weight: 800; font-size: 0.85rem; }
    .unread-dot {
      position: absolute; top: -6px; right: -6px;
      background: #ff4d4d; color: #fff; font-size: 0.65rem;
      width: 18px; height: 18px; border-radius: 50%;
      display: flex; align-items: center; justify-content: center; font-weight: 800;
    }

    /* ── Chat Panel ── */
    .ai-chat-panel {
      position: absolute; bottom: 68px; left: 0;
      width: 390px; max-height: 580px;
      background: #0d1426; border: 1px solid rgba(0,255,135,0.2);
      border-radius: 20px; overflow: hidden;
      display: flex; flex-direction: column;
      box-shadow: 0 24px 60px rgba(0,0,0,0.7);
      animation: slideUp 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
    }
    @keyframes slideUp {
      from { opacity: 0; transform: translateY(20px) scale(0.95); }
      to   { opacity: 1; transform: translateY(0) scale(1); }
    }

    /* ── Header ── */
    .chat-header {
      display: flex; align-items: center; justify-content: space-between;
      padding: 0.85rem 1rem;
      background: linear-gradient(135deg, rgba(0,255,135,0.08), rgba(0,200,255,0.05));
      border-bottom: 1px solid rgba(0,255,135,0.12);
      gap: 0.5rem;
    }
    .agent-info { display: flex; align-items: center; gap: 0.75rem; flex: 1; min-width: 0; }
    .agent-avatar {
      width: 44px; height: 44px; border-radius: 50%;
      background: linear-gradient(135deg, #00ff87, #00c8ff);
      display: flex; align-items: center; justify-content: center;
      color: #0a0f1e; font-size: 1.1rem; position: relative;
      flex-shrink: 0; overflow: hidden;
    }
    .avatar-img { width: 100%; height: 100%; object-fit: cover; border-radius: 50%; }
    .status-dot {
      position: absolute; bottom: 2px; right: 2px;
      width: 10px; height: 10px; border-radius: 50%;
      background: #22c55e; border: 2px solid #0d1426;
      animation: pulse-dot 2s infinite; z-index: 1;
    }
    @keyframes pulse-dot {
      0%, 100% { box-shadow: 0 0 0 0 rgba(34,197,94,0.4); }
      50%       { box-shadow: 0 0 0 4px rgba(34,197,94,0); }
    }
    .agent-name { font-size: 0.95rem; font-weight: 800; color: #00ff87; }
    .agent-title { font-size: 0.7rem; color: rgba(255,255,255,0.45); }

    /* ── New Chat Button ── */
    .new-chat-btn {
      display: flex; align-items: center; gap: 0.3rem;
      background: rgba(0,255,135,0.1); border: 1px solid rgba(0,255,135,0.25);
      color: #00ff87; border-radius: 20px; padding: 0.3rem 0.65rem;
      font-size: 0.72rem; font-weight: 700; cursor: pointer;
      font-family: 'Cairo', sans-serif; transition: all 0.2s;
      flex-shrink: 0;
      i { font-size: 0.7rem; }
      &:hover { background: rgba(0,255,135,0.2); border-color: rgba(0,255,135,0.4); }
    }

    /* ── Suggestions ── */
    .suggestions-panel { padding: 1rem; }
    .greeting {
      font-size: 0.83rem; color: rgba(255,255,255,0.8); line-height: 1.5;
      margin-bottom: 0.85rem; background: rgba(0,255,135,0.05);
      border: 1px solid rgba(0,255,135,0.1); border-radius: 10px; padding: 0.75rem;
    }
    .chips-label { font-size: 0.72rem; color: rgba(255,255,255,0.4); margin-bottom: 0.5rem; }
    .chips { display: flex; flex-wrap: wrap; gap: 0.4rem; }
    .chip {
      background: rgba(0,255,135,0.07); border: 1px solid rgba(0,255,135,0.2);
      color: rgba(255,255,255,0.8); border-radius: 20px;
      padding: 0.3rem 0.75rem; font-size: 0.75rem; cursor: pointer;
      transition: all 0.2s; font-family: 'Cairo', sans-serif;
      &:hover { background: rgba(0,255,135,0.15); color: #fff; }
    }

    /* ── Messages ── */
    .messages-area {
      flex: 1; overflow-y: auto; padding: 0.75rem 1rem;
      display: flex; flex-direction: column; gap: 0.75rem;
      max-height: 320px;
      scrollbar-width: thin; scrollbar-color: rgba(0,255,135,0.2) transparent;
    }
    .message { display: flex; gap: 0.5rem; align-items: flex-end; }
    .message.user { flex-direction: row-reverse; }
    .msg-avatar {
      width: 30px; height: 30px; border-radius: 50%; flex-shrink: 0;
      background: linear-gradient(135deg, #00ff87, #00c8ff);
      display: flex; align-items: center; justify-content: center;
      color: #0a0f1e; font-size: 0.75rem; overflow: hidden;
    }
    .avatar-img-sm { width: 100%; height: 100%; object-fit: cover; border-radius: 50%; }
    .msg-bubble {
      max-width: 75%; padding: 0.6rem 0.9rem; border-radius: 16px;
      font-size: 0.82rem; line-height: 1.5; color: rgba(255,255,255,0.9);
    }
    .message.agent .msg-bubble {
      background: rgba(255,255,255,0.06);
      border: 1px solid rgba(255,255,255,0.08);
      border-bottom-left-radius: 4px;
    }
    .message.user .msg-bubble {
      background: linear-gradient(135deg, rgba(0,255,135,0.15), rgba(0,200,255,0.1));
      border: 1px solid rgba(0,255,135,0.2);
      border-bottom-right-radius: 4px; text-align: right;
    }
    .msg-time { font-size: 0.65rem; color: rgba(255,255,255,0.3); margin-top: 0.25rem; }

    /* ── Typing ── */
    .typing-dots {
      display: flex; gap: 4px; align-items: center; padding: 0.25rem 0;
      span {
        width: 7px; height: 7px; border-radius: 50%;
        background: #00ff87; animation: bounce 1.2s infinite;
        &:nth-child(2) { animation-delay: 0.2s; }
        &:nth-child(3) { animation-delay: 0.4s; }
      }
    }
    @keyframes bounce {
      0%, 60%, 100% { transform: translateY(0); }
      30%           { transform: translateY(-6px); }
    }

    /* ── Input ── */
    .chat-input-area {
      display: flex; gap: 0.5rem; padding: 0.75rem 1rem;
      border-top: 1px solid rgba(255,255,255,0.06);
      background: rgba(0,0,0,0.2);
    }
    .chat-input {
      flex: 1; background: rgba(255,255,255,0.05); border: 1px solid rgba(255,255,255,0.1);
      border-radius: 10px; color: #fff; padding: 0.55rem 0.85rem;
      font-size: 0.82rem; font-family: 'Cairo', sans-serif;
      outline: none; transition: border-color 0.2s;
      &:focus { border-color: rgba(0,255,135,0.4); }
      &::placeholder { color: rgba(255,255,255,0.3); }
    }
    .send-btn {
      background: linear-gradient(135deg, #00ff87, #00c8ff);
      color: #0a0f1e; border: none; border-radius: 10px;
      padding: 0 1rem; height: 38px; cursor: pointer;
      font-size: 0.82rem; font-weight: 800;
      display: flex; align-items: center; gap: 0.35rem;
      transition: all 0.2s; flex-shrink: 0;
      font-family: 'Cairo', sans-serif;
      i { font-size: 0.85rem; }
      .send-label { white-space: nowrap; }
      &:hover:not(:disabled) { transform: scale(1.04); box-shadow: 0 4px 16px rgba(0,255,135,0.4); }
      &:disabled { opacity: 0.5; cursor: not-allowed; }
    }
  `]
})
export class AiChatWidgetComponent implements AfterViewChecked {
  @ViewChild('messagesArea') messagesArea!: ElementRef;

  private readonly aiService = inject(AiAgentService);

  isOpen      = signal(false);
  isLoading   = signal(false);
  unread      = signal(0);
  messages    = signal<ChatMessage[]>([]);
  avatarError = signal(false);

  userInput = '';

  readonly suggestionChips = [
    'من هو أفضل مهاجم متاح؟',
    'رشح لي لاعب جناح أيسر',
    'حلل لي أداء اللاعبين',
    'ما هي معايير التكشيف المثلى؟',
    'ما عدد اللاعبين المسجلين؟',
  ];

  toggleChat(): void {
    this.isOpen.update(v => !v);
    if (this.isOpen()) this.unread.set(0);
  }

  /** مسح المحادثة وبدء جديدة */
  newChat(): void {
    this.messages.set([]);
    this.userInput = '';
    this.isLoading.set(false);
  }

  sendChip(text: string): void {
    this.userInput = text;
    this.sendMessage();
  }

  sendMessage(): void {
    const msg = this.userInput.trim();
    if (!msg || this.isLoading()) return;
    this.userInput = '';

    this.messages.update(m => [...m, { role: 'user', text: msg, timestamp: new Date() }]);
    this.isLoading.set(true);
    this.messages.update(m => [...m, { role: 'agent', text: '', timestamp: new Date(), loading: true }]);

    this.aiService.chat({ message: msg }).subscribe({
      next: (res) => {
        const reply = res.data?.reply ?? 'عذرًا، لم أتمكن من الإجابة.';
        this.messages.update(msgs => {
          const updated = [...msgs];
          updated[updated.length - 1] = { role: 'agent', text: reply, timestamp: new Date(), loading: false };
          return updated;
        });
        this.isLoading.set(false);
        if (!this.isOpen()) this.unread.update(n => n + 1);
      },
      error: () => {
        this.messages.update(msgs => {
          const updated = [...msgs];
          updated[updated.length - 1] = {
            role: 'agent',
            text: 'تعذّر الاتصال بالمساعد الذكي. تأكد من تشغيل الخادم وحاول مرة أخرى.',
            timestamp: new Date(), loading: false
          };
          return updated;
        });
        this.isLoading.set(false);
      }
    });
  }

  formatText(text: string): string {
    return text
      .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
      .replace(/\*(.*?)\*/g, '<em>$1</em>')
      .replace(/①|②|③|④|⑤|⑥/g, m => `<br><strong>${m}</strong>`)
      .replace(/━━.*?━━/g, m => `<strong style="color:#00ff87">${m}</strong>`)
      .replace(/\n/g, '<br>');
  }

  ngAfterViewChecked(): void {
    this.scrollToBottom();
  }

  private scrollToBottom(): void {
    try {
      if (this.messagesArea) {
        this.messagesArea.nativeElement.scrollTop = this.messagesArea.nativeElement.scrollHeight;
      }
    } catch {}
  }
}
