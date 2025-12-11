import {Media} from './media';
import {Terminal} from './terminal';
import {Car} from './car';
import {RefreshToken} from './refresh-token';

export interface User {
  readonly idUser?: number;
  surnameUser: string;
  firstName: string;
  pseudo: string;
  emailUser: string;
  passwordUser?: string;
  role?: string;
  dateOfBirth: string;
  phone: string;
  iban?: string;
  banished?: boolean;
  media?: Media;
  bornes?: Terminal[];
  vehicule?: Car[];
  refreshTokens?: RefreshToken[];
}

export interface UserLogin {
  emailUser: string;
  passwordUser: string;
}
