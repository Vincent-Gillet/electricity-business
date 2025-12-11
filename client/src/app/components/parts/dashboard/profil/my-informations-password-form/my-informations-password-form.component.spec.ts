import { ComponentFixture, TestBed } from '@angular/core/testing';

import { MyInformationsPasswordFormComponent } from './my-informations-password-form.component';

describe('MyInformationsPasswordFormComponent', () => {
  let component: MyInformationsPasswordFormComponent;
  let fixture: ComponentFixture<MyInformationsPasswordFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MyInformationsPasswordFormComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(MyInformationsPasswordFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
