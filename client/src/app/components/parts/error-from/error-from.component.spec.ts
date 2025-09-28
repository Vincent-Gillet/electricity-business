import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ErrorFromComponent } from './error-from.component';

describe('ErrorFromComponent', () => {
  let component: ErrorFromComponent;
  let fixture: ComponentFixture<ErrorFromComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ErrorFromComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ErrorFromComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
