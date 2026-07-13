import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { timeout, catchError } from 'rxjs/operators';
import { environment } from '../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ChatService {
  private apiUrl = `${environment.apiUrl}/api/chat`;

  constructor(private http: HttpClient) {}

  sendMessage(message: string, chatId?: string): Observable<any> {
    const payload = { message, chatId };
    return this.http.post<any>(this.apiUrl, payload).pipe(
      timeout(20000), // 20 seconds timeout
      catchError(err => {
        return throwError(() => err);
      })
    );
  }

  clearHistory(chatId: string): Observable<any> {
    return this.http.delete(`${this.apiUrl}/history/${chatId}`);
  }
}
