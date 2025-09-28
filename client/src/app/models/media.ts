import {Option} from './option';
import {User} from './user';
import {Terminal} from './terminal';
import {Place} from './place';

export interface Media {
  readonly idMedia?: number;
  nameMedia: string;
  url: string;
  type: string;
  descriptionMedia?: string;
  size?: string;
  dateCreation?: string;
  options?: Option;
  terminal?: Terminal;
  places?: Place[];
  user?: User;
}
