import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Assetmanagement } from './assetmanagement';

describe('Assetmanagement', () => {
  let component: Assetmanagement;
  let fixture: ComponentFixture<Assetmanagement>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Assetmanagement],
    }).compileComponents();

    fixture = TestBed.createComponent(Assetmanagement);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
