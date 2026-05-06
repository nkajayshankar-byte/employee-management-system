import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';

interface AssetDTO {
  id?: string;
  employeeId: string;
  employeeName?: string;
  assetName: string;
  assetType: string;
  serialNumber: string;
  status: string;
  assignedDate: Date;
  returnDate?: Date;
  conditions: string;
  description:string;
  remarks: string;
}

@Injectable({
  providedIn: 'root'
})
export class AssetService {
  private apiUrl = `${environment.apiUrl}/api/admin/assets`;

  constructor(private http: HttpClient) {}

  getAllAssets(): Observable<AssetDTO[]> {
    return this.http.get<AssetDTO[]>(this.apiUrl);
  }

  getAssetById(id: string): Observable<AssetDTO> {
    return this.http.get<AssetDTO>(`${this.apiUrl}/${id}`);
  }

  getAssetsByEmployee(employeeId: string): Observable<AssetDTO[]> {
    return this.http.get<AssetDTO[]>(`${this.apiUrl}/employee/${employeeId}`);
  }

  searchAssets(searchTerm: string): Observable<AssetDTO[]> {
    return this.http.get<AssetDTO[]>(`${this.apiUrl}/search/${searchTerm}`);
  }

   createAsset(asset: AssetDTO): Observable<any> {
    return this.http.post(this.apiUrl, asset, {
      responseType: 'text' as 'json'   // 🔥 prevents 200 OK error issue
    });
  }

  updateAsset(id: string, asset: AssetDTO): Observable<any> {
    return this.http.put(`${this.apiUrl}/${id}`, asset);
  }

  deleteAsset(id: string): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`);
  }
}