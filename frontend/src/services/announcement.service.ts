import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AnnouncementService {
  private apiUrl = `${environment.apiUrl}/api/admin/announcements`;

  constructor(private http: HttpClient) { }

  scheduleAnnouncement(announcement: any): Observable<any> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
    // Use responseType text since backend returns a plain string
    return this.http.post(`${this.apiUrl}/schedule`, announcement, { headers, responseType: 'text' as 'json' });
  }

  generateContent(subject: string, targetAudience: string): Observable<any> {
    const token = localStorage.getItem('token');
    const headers = new HttpHeaders({
      'Authorization': `Bearer ${token}`
    });
    return this.http.post(`${this.apiUrl}/generate`, { subject, targetAudience }, { headers });
  }
}
