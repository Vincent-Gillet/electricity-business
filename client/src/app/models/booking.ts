import {User} from './user';
import {Car} from './car';
import {Terminal} from './terminal';
import {Option} from './option';
import {Address} from './address';

export interface Booking {
  readonly idBooking?: number;
  readonly publicId?: string;
  numberBooking?: string;
  statusBooking?: string;
  totalAmount?: number;
  paymentDate?: string;
  startingDate: string;
  endingDate: string;

  user?: User;
  userClientDTO?: User;
  userOwnerDTO?: User;
  addressDTO?: Address;
  publicIdCar?: Car;
  publicIdTerminal?: Terminal;
  publicIdOption?: Option;

  orderBooking?: string;
}

export interface BookingStatus {
  statusBooking?: string;
}
