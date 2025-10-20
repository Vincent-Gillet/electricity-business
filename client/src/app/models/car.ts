import {User} from './user';

export interface Car {
  readonly publicId?: string;
  licensePlate: string;
  brand: string;
  model: string;
  year: string;
  batteryCapacity: number;
  user?: User;
}

