import {User} from './user';
import {Car} from './car';
import {Terminal} from './terminal';
import {Option} from './option';

export interface Booking {
  readonly idBooking?: number;
  numberBooking: string;
  statusBooking: string;
  totalAmount: number;
  paymentDate: string;
  startingDate: string;
  endingDate: string;

  user?: User;
  car?: Car;
  terminal?: Terminal;
  option?: Option;
}
