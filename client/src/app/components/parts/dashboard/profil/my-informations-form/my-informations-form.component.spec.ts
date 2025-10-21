import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MyInformationsFormComponent } from './my-informations-form.component';

describe('MyInformationsFormComponent', () => {
  let component: MyInformationsFormComponent;
  let fixture: ComponentFixture<MyInformationsFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MyInformationsFormComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MyInformationsFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
