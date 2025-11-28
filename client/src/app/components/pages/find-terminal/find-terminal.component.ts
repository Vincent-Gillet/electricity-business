import {Component, inject, OnInit} from '@angular/core';
import {GeolocationService} from '@ng-web-apis/geolocation';
import {FormBuilder, FormGroup, ReactiveFormsModule} from '@angular/forms';
import {Router} from '@angular/router';
import {MapTerminalComponent} from '../../parts/dashboard/map-terminal/map-terminal.component';
import {LocationSuggestion, NominatimService} from '../../../services/map/nominatim.service';
import {TerminalService} from '../../../services/terminal/terminal.service';
import {debounceTime, distinctUntilChanged} from 'rxjs';

@Component({
  selector: 'app-find-terminal',
  standalone: true,
  imports: [
    MapTerminalComponent,
    ReactiveFormsModule
  ],
  templateUrl: './find-terminal.component.html',
  styleUrl: './find-terminal.component.scss'
})
export class FindTerminalComponent implements OnInit {


  nominatimService: NominatimService = inject(NominatimService);
  searchTerminalForm: FormGroup;


  // Propriété pour stocker les bornes
  terminals: any[] = [];

  // Injection du service
  private terminalService: TerminalService= inject(TerminalService)
  private geolocation: GeolocationService = inject(GeolocationService);

  long: number = 2.287592;
  lat: number = 48.862725;

  locationSuggestions: LocationSuggestion[] = [];
  showSuggestions: boolean = false;
  isLoading: boolean = false;

  public startingDateSearch: Date = new Date();
  public endingDateSearch: number;

  public minDate: Date = new Date();
  public maxDate: Date = new Date(this.minDate.getTime() + (1000 * 60 * 60 * 24 * 7 * 4)); // 4 semaines

  minDateTimeString = this.formatDateForInput(this.minDate);
  maxDateTimeString = this.formatDateForInput(this.maxDate);

  formatDateForInput(date: Date): string {
    const local = new Date(date);
    local.setMinutes(local.getMinutes() - local.getTimezoneOffset()); // Ajuste le fuseau
    return local.toISOString().slice(0, 16); // yyyy-MM-ddTHH:mm
  }

  constructor(private fb: FormBuilder, private router: Router) {
    //Création du form
    this.searchTerminalForm = this.fb.group(
      {
        address: [''],
        radius: [''],
        occupied: [true],
        startingDate: [''],
        endingDate: ['']
      }
    );
  }


  ngOnInit() {

    console.log("startingDateSearch : " + this.startingDateSearch);
    console.log("endingDateSearch : " + this.endingDateSearch);

    this.initializeGeolocation();

    this.searchTerminalForm.get('address')?.valueChanges.pipe(
      debounceTime(300),
      distinctUntilChanged()
    ).subscribe(() => {
      this.onAddressInput();
    });
  }

  private initializeGeolocation() {
    this.geolocation.subscribe({
      next: (data) => {
        this.lat = data.coords.latitude;
        this.long = data.coords.longitude;
        this.chargerTerminals(this.getDefaultParams());
      },
      error: (error) => {
        console.error('Erreur de géolocalisation:', error);
        this.lat = 48.8566; // Paris
        this.long = 2.3522; // Paris
        this.chargerTerminals(this.getDefaultParams());
      }
    });
  }


  private buildParams(long: number, lat: number): any {
    const formValues = { ...this.searchTerminalForm.value };
    const params = {
      longitude: long,
      latitude: lat,
      ...formValues
    };
    params.radius = (params.radius === '' || params.radius === null || params.radius === undefined) ? 5 : params.radius;
    params.occupied = formValues.occupied === true ? false : undefined;
    // Suppression des champs vides
    if (!params.address) delete params.address;
    if (!params.startingDate) delete params.startingDate;
    if (!params.endingDate) delete params.endingDate;

    return params;
  }


  onSubmit() {
    if (this.searchTerminalForm.value.address) {

      const searchTerminalData = this.searchTerminalForm.value;

      if(searchTerminalData.startingDate && searchTerminalData.endingDate) {
        this.startingDateSearch = new Date(searchTerminalData.startingDate);
        this.endingDateSearch = parseInt(this.searchTerminalForm.value.endingDate, 10);

        const durationMinutes = parseInt(searchTerminalData.endingDate, 10);
        const startDate = new Date(searchTerminalData.startingDate);
        const endDate = new Date(startDate.getTime() + durationMinutes * 60 * 1000);

        // Conversion en ISO locale avec décalage horaire
        const pad = (n: number) => n.toString().padStart(2, '0');
        searchTerminalData.endingDate =
          `${endDate.getFullYear()}-${pad(endDate.getMonth()+1)}-${pad(endDate.getDate())}` +
          `T${pad(endDate.getHours())}:${pad(endDate.getMinutes())}`;

      }

      console.log("MON FORM EST SOUMIS");
      console.log("loginForm.valid ", this.searchTerminalForm.valid);
      console.log("Toutes les valeurs des control du groupe -> loginForm.value ",searchTerminalData);

      console.log("this.searchBorneForm.value : ", searchTerminalData);

      const address = searchTerminalData.address;

      const encodeAddress = encodeURIComponent(address).replace(/%20/g, '+');
      console.log("encodeAddress : ", encodeAddress);
      this.nominatimService.getGeoLocationWithAdress(encodeAddress).subscribe(
        {
          next: (response) => {
            this.long = response.features[0].geometry.coordinates[0];
            this.lat = response.features[0].geometry.coordinates[1];
            const params = this.buildParams(this.long, this.lat);
            this.chargerTerminals(params);
          },
          error: (error) => {
            console.error('Error login user:', error);
          }
        }
      )
    } else {
      this.initializeGeolocation();
    }



  };


  getDefaultParams() {
    return {
      longitude : this.long,
      latitude : this.lat,
      radius : !this.searchTerminalForm.get('radius')?.value ? 50 : this.searchTerminalForm.get('radius')?.value,
      occupied : this.searchTerminalForm.get('occupied')?.value === true ? false : undefined
    };
  }



  // Méthode pour charger les bornes
  chargerTerminals(paramsObj: any) {
    console.log("paramsObj : ", paramsObj);
    const params = Object.entries(paramsObj)
      .map(([key, value]) => {
        if (value !== null && value !== undefined && value !== '') {
          return `${key}=${value}`;
        }
        return undefined;
      })
      .filter(Boolean)
      .join('&');
    console.log("params : ", params);

    // Appel du service pour récupérer les bornes
    this.terminalService.getTerminalsNearby(params).subscribe(data => {
      //Affectation des datas
      this.terminals = data;
    });
  }

  onReset() {
    this.searchTerminalForm.reset();
    this.initializeGeolocation();
  }


  /**
   * Recherche d'adresse au fur et à mesure de la saisie
   */
  onAddressInput(): void {
    const address = this.searchTerminalForm.get('address')?.value;
    if (!address || address.length < 3) {
      this.locationSuggestions = [];
      this.showSuggestions = false;
      return;
    }

    this.isLoading = true;
    this.showSuggestions = true;

    this.nominatimService.searchLocations(address).subscribe({
      next: (locations) => {
        this.locationSuggestions = locations;
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Erreur lors de la recherche:', error);
        this.isLoading = false;
      }
    });
  }

  /**
   * Sélection d'une suggestion d'adresse
   */
  selectSuggestion(suggestion: LocationSuggestion): void {
    this.searchTerminalForm.patchValue({
      address: suggestion.display_name
    });
    this.lat = parseFloat(suggestion.lat);
    this.long = parseFloat(suggestion.lon);
    this.showSuggestions = false;
    this.chargerTerminals(this.getDefaultParams());
  }

  closeSuggestions(): void {
    setTimeout(() => {
      this.showSuggestions = false;
    }, 200);
  }

}
