import { ChangeDetectionStrategy, Component, signal, ElementRef, ViewChild, AfterViewChecked, HostListener, inject } from '@angular/core';
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
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [CommonModule, FormsModule],
  templateUrl: './ai-chat-widget.component.html',
  styleUrls: ['./ai-chat-widget.component.scss']
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

  @HostListener('document:keydown.escape')
  public handleEscape(): void {
    if (this.isOpen()) {
      this.isOpen.set(false);
    }
  }

  toggleChat(): void {
    this.isOpen.update(v => !v);
    if (this.isOpen()) this.unread.set(0);
  }

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
