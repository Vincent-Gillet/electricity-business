import {Media} from './media';

export interface Option {
  readonly idOption?: number;
  readonly publicId?: string;
  nameOption: string;
  priceOption: number;
  descriptionOption: string;
  media?: Media;
  publicIdPlace?: string;
}
