import { Component, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AnnouncementService } from '../../services/announcement.service';
import { ToastrService } from 'ngx-toastr';

@Component({
  selector: 'app-admin-announcement',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-announcement.html',
  styleUrls: ['./admin-announcement.css']
})
export class AdminAnnouncementComponent {
  announcement = {
    subject: '',
    content: '',
    scheduledTime: '',
    targetAudience: 'Organization' // Default
  };

  audiences = ['Admins', 'Users', 'Employees', 'Organization'];
  isLoading = false;
  isGenerating = false;

  constructor(
    private announcementService: AnnouncementService,
    private toastr: ToastrService,
    private cdr: ChangeDetectorRef
  ) {}

  schedule() {
    if (!this.announcement.subject || !this.announcement.content || !this.announcement.scheduledTime) {
      this.toastr.warning('Please fill in all required fields.');
      return;
    }

    // Format datetime string for backend: 'YYYY-MM-DD HH:mm:ss'
    const dateObj = new Date(this.announcement.scheduledTime);
    const formattedDate = dateObj.toISOString().slice(0, 19).replace('T', ' ');

    const payload = {
      ...this.announcement,
      scheduledTime: formattedDate
    };

    this.isLoading = true;
    this.cdr.detectChanges(); // Trigger change detection manually as requested

    this.announcementService.scheduleAnnouncement(payload).subscribe({
      next: (res) => {
        this.toastr.success('Announcement scheduled successfully!');
        this.announcement = { subject: '', content: '', scheduledTime: '', targetAudience: 'Organization' };
        this.isLoading = false;
        this.cdr.detectChanges(); // Trigger change detection
      },
      error: (err) => {
        console.error(err);
        this.toastr.error('Failed to schedule announcement.');
        this.isLoading = false;
        this.cdr.detectChanges(); // Trigger change detection
      }
    });
  }

  generateAIContent() {
    if (!this.announcement.subject) {
      this.toastr.warning('Please enter a subject first.');
      return;
    }

    this.isGenerating = true;
    this.cdr.detectChanges();

    this.announcementService.generateContent(this.announcement.subject, this.announcement.targetAudience).subscribe({
      next: (res) => {
        this.announcement.content = res.content;
        this.toastr.success('AI Content generated successfully!');
        this.isGenerating = false;
        this.cdr.detectChanges();
      },
      error: (err) => {
        console.error(err);
        this.toastr.error('Failed to generate AI content. Ensure your Groq API key is set.');
        this.isGenerating = false;
        this.cdr.detectChanges();
      }
    });
  }
}
