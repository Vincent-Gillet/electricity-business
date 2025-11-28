import { Injectable } from '@angular/core';
import {map, Observable} from 'rxjs';
import {HttpClient} from '@angular/common/http';

export interface LocationSuggestion {
  display_name: string;
  lat: string;
  lon: string;
}

@Injectable({
  providedIn: 'root'
})
export class NominatimService {

  private apiUrl: string = "https://nominatim.openstreetmap.org/search?q="
  constructor(private http: HttpClient) {}

  getGeoLocationWithAdress(param: string): Observable<any> {
    return this.http.get(`${this.apiUrl}${param}&format=geojson`);
  }

  searchLocations(query: string, limit: number = 5): Observable<LocationSuggestion[]> {
    const params = {
      q: query,
      format: 'json',
      addressdetails: '1',
      limit: limit.toString()
    };

    return this.http.get<any[]>(this.apiUrl, { params }).pipe(
      map(results => results.map(result => ({
        display_name: result.display_name,
        lat: result.lat,
        lon: result.lon
      })))
    );
  }
}
