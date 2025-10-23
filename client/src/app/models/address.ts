import {User} from './user';
import {Place} from './place';

export interface Address {
  readonly publicId?: string;
  nameAddress: string;
  address: string;
  postCode: string;
  city: string;
  region: string;
  country: string;
  complement?: string;
  floor?: string;
  user?: User;
  place?: Place;
}
