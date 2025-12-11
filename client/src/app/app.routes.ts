import { Routes } from '@angular/router';
import {HomeComponent} from './components/pages/home/home.component';
import {DashboardUserComponent} from './components/pages/dashboard-user/dashboard-user.component';
import {StatisticsComponent} from './components/parts/dashboard/statistics/statistics.component';
import {SubscriptionComponent} from './components/pages/subscription/subscription.component';
import {LoginComponent} from './components/pages/login/login.component';
import {CguComponent} from './components/pages/cgu/cgu.component';
import {LegalNoticesComponent} from './components/pages/legal-notices/legal-notices.component';
import {CarFormComponent} from './components/parts/dashboard/car-form/car-form.component';
import {MyInformationsComponent} from './components/parts/dashboard/profil/my-informations/my-informations.component';
import {AddressesComponent} from './components/parts/dashboard/profil/addresses/addresses.component';
import { authGuard } from './auth/auth.guard';
import {FindTerminalComponent} from './components/pages/find-terminal/find-terminal.component';
import {CarsComponent} from './components/pages/cars/cars.component';
import {OptionsComponent} from './components/pages/options/options.component';
import {MyBookingsComponent} from './components/pages/my-bookings/my-bookings.component';
import {BookingRequestsComponent} from './components/pages/booking-requests/booking-requests.component';
import {MyRechargingPointsComponent} from './components/pages/my-recharging-points/my-recharging-points.component';

export const routes: Routes = [
  { path: '', component: HomeComponent, canActivate: [() => true] },
  { path: 'tableau-de-bord', component: DashboardUserComponent, canActivate: [authGuard],
    children: [
      { path: '', component: StatisticsComponent },
      { path: 'trouver-bornes', component: FindTerminalComponent },
      { path: 'mes-reservations', component: MyBookingsComponent },
      { path: 'mes-demandes-de-reservation', component: BookingRequestsComponent },
      { path: 'lieux-de-recharge', component: MyRechargingPointsComponent,
        children: [{
          path: 'nouveau-lieu', component: CarFormComponent
        }]
      },
      { path: 'mes-voitures', component: CarsComponent,
        children: [{
          path: 'nouvelle-voiture', component: CarFormComponent
        }]
      },
      { path: 'mes-options', component: OptionsComponent},
      { path: 'mes-informations', component: MyInformationsComponent },
      { path: 'mes-adresses', component: AddressesComponent },
    ]
  },
  { path: 'inscription', component: SubscriptionComponent, canActivate: [() => true] },
  { path: 'connexion', component: LoginComponent, canActivate: [() => true] },
  { path: 'cgu', component: CguComponent, canActivate: [() => true] },
  { path: 'mention-legales', component: LegalNoticesComponent, canActivate: [() => true] },
  { path: '**', redirectTo: '' }
];
