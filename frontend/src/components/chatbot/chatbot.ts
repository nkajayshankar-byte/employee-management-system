import { Component, ElementRef, ViewChild, AfterViewChecked, ChangeDetectorRef, OnDestroy, HostListener, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChatService } from '../../services/chat.service';
import { MarkdownPipe } from '../../pipes/markdown.pipe';
import { AuthService } from '../../services/auth';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-chatbot',
  standalone: true,
  imports: [CommonModule, FormsModule, MarkdownPipe],
  templateUrl: './chatbot.html',
  styleUrls: ['./chatbot.css']
})
export class ChatbotComponent implements AfterViewChecked, OnDestroy, OnInit {
  @ViewChild('scrollMe') private myScrollContainer!: ElementRef;
  @ViewChild('chatbotContainer') private chatbotContainer!: ElementRef;

  private authSubscription?: Subscription;
  isOpen = false;
  userInput = '';
  messages: { text: string; sender: 'user' | 'ai' }[] = [
    { text: 'Hello! I am your HR Assistant. How can I help you today?', sender: 'ai' }
  ];
  isLoading = false;
  chatId = '';
  editingIndex: number | null = null;
  editInput = '';

  constructor(
    private chatService: ChatService,
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ) { }

  ngOnInit() {
    // Clear chat when user logs out or changes
    this.authSubscription = this.authService.currentUser$.subscribe(user => {
      this.messages = [
        { text: 'Hello! I am your HR Assistant. How can I help you today?', sender: 'ai' }
      ];
      this.chatId = this.generateChatId();
      this.cdr.detectChanges();
    });
  }

  private generateChatId(): string {
    return 'chat_' + Math.random().toString(36).substring(2, 11);
  }

  ngAfterViewChecked() {
    this.scrollToBottom();
  }

  ngOnDestroy() {
    if (this.authSubscription) {
      this.authSubscription.unsubscribe();
    }
  }

  @HostListener('document:click', ['$event'])
  onDocumentClick(event: MouseEvent) {
    if (this.isOpen && this.chatbotContainer) {
      const clickedInside = this.chatbotContainer.nativeElement.contains(event.target);
      if (!clickedInside) {
        this.isOpen = false;
        this.cdr.detectChanges();
      }
    }
  }

  toggleChat(event?: Event) {
    if (event) {
      event.stopPropagation();
    }
    this.isOpen = !this.isOpen;
    if (this.isOpen) {
      setTimeout(() => this.scrollToBottom(), 100);
    }
  }

  sendMessage(customMessage?: string) {
    const userMsg = customMessage || this.userInput;
    if (!userMsg.trim() || this.isLoading) return;

    if (!customMessage) {
      this.messages.push({ text: userMsg, sender: 'user' });
      this.userInput = '';
    }
    this.isLoading = true;

    this.chatService.sendMessage(userMsg, this.chatId).subscribe({
      next: (res) => {
        this.messages.push({ text: res.response, sender: 'ai' });
        this.isLoading = false;
        this.cdr.detectChanges(); // Force UI update
        setTimeout(() => this.scrollToBottom(), 100);
      },
      error: (err) => {
        console.error(err);
        let errorMsg = 'Sorry, something went wrong. Please try again.';
        if (err.status === 400 || err.status === 429) {
          errorMsg = 'AI is busy (Rate Limit). Please wait a moment before trying again.';
        }
        this.messages.push({ text: errorMsg, sender: 'ai' });
        this.isLoading = false;
        this.cdr.detectChanges(); // Force UI update
      }
    });
  }

  editMessage(index: number) {
    this.editingIndex = index;
    this.editInput = this.messages[index].text;
  }

  saveEdit(index: number) {
    if (!this.editInput.trim()) return;

    // Truncate messages from this point
    this.messages = this.messages.slice(0, index);
    
    const editedMsg = this.editInput;
    this.messages.push({ text: editedMsg, sender: 'user' });
    this.editingIndex = null;
    this.editInput = '';

    // Clear backend history for this chat to start fresh branch
    this.chatService.clearHistory(this.chatId).subscribe({
      next: () => {
        this.sendMessage(editedMsg);
      },
      error: (err) => {
        console.error('Failed to clear history:', err);
        // Still try to send, though context might be mixed
        this.sendMessage(editedMsg);
      }
    });
  }

  cancelEdit() {
    this.editingIndex = null;
    this.editInput = '';
  }

  private scrollToBottom(): void {
    try {
      if (this.myScrollContainer && this.myScrollContainer.nativeElement) {
        this.myScrollContainer.nativeElement.scrollTop = this.myScrollContainer.nativeElement.scrollHeight;
      }
    } catch (err) { }
  }
}
