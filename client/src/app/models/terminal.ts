import {User} from './user';
import {Media} from './media';
import {Place} from './place';
import {Repairer} from './repairer';

export interface Terminal {
  readonly idTerminal?: number;
  readonly publicId?: string;
  nameTerminal: string;
  latitude: number;
  longitude: number;
  price: number;
  power: number;
  instructionTerminal: string;
  standing?: boolean;
  statusTerminal?: string;
  occupied?: boolean;
  dateCreation?: string;
  dateModification?: string;
  user?: User;
  medias?: Media[];
  place?: Place;
  repairer?: Repairer;
  publicIdPlace?: string;
}
