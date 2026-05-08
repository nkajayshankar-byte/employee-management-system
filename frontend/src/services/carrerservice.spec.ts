import { TestBed } from '@angular/core/testing';

import { Carrerservice } from './carrerservice';

describe('Carrerservice', () => {
  let service: Carrerservice;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Carrerservice);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
