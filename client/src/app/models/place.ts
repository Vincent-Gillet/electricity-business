import {User} from './user';
import {Media} from './media';
import {Terminal} from './terminal';

export interface Place {
  readonly idPlace?: number;
  instructionPlace: string;
  user?: User;
  media?: Media[];
  terminal?: Terminal[];

}
