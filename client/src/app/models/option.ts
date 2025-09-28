import {Media} from './media';

export interface Option {
  readonly idOption?: number;
  nameOption: string;
  priceOption: number;
  descriptionOption: string;
  media?: Media;
}
