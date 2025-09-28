import { Routes } from '@angular/router';
import {HomeComponent} from './components/pages/home/home.component';
import {DashboardUserComponent} from './components/pages/dashboard-user/dashboard-user.component';
import {StatisticsComponent} from './components/parts/dashboard/statistics/statistics.component';
import {FindTerminalComponent} from './components/parts/dashboard/find-terminal/find-terminal.component';
import {CarsComponent} from './components/parts/dashboard/cars/cars.component';
import {MyRechargingPointsComponent} from './components/parts/dashboard/my-recharging-points/my-recharging-points.component';
import {SubscriptionComponent} from './components/pages/subscription/subscription.component';
import {LoginComponent} from './components/pages/login/login.component';
import {CguComponent} from './components/pages/cgu/cgu.component';
import {LegalNoticesComponent} from './components/pages/legal-notices/legal-notices.component';
import {MyBookingsComponent} from './components/parts/dashboard/my-bookings/my-bookings.component';
import {BookingRequestsComponent} from './components/parts/dashboard/booking-requests/booking-requests.component';
import {CarFormComponent} from './components/parts/dashboard/car-form/car-form.component';

export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'tableau-de-bord', component: DashboardUserComponent,
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
    ]
  },
  /*
    { path: 'trouver-borne', component: FindTerminalComponent },
  */
  { path: 'inscription', component: SubscriptionComponent },
  { path: 'connexion', component: LoginComponent },
  { path: 'cgu', component: CguComponent },
  { path: 'mention-legales', component: LegalNoticesComponent },
  { path: '**', redirectTo: '' }// Route wildcard pour 404
];
