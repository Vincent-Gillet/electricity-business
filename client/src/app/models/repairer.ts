import {Terminal} from './terminal';

export interface Repairer {
  readonly idRepairer?: number;
  nameRepairer: string;
  emailRepairer: string;
  passwordRepairer?: string;
  role?: string;
  terminal?: Terminal[];

}
