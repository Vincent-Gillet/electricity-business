import {User} from './user';

export interface Car {
  readonly idCar?: number;
  licensePlate: string;
  brand: string;
  model: string;
  year: string;
  batteryCapacity: number;
  user?: User;
}

