import { Component } from '@angular/core';
import { DialogConfig } from '../dialog-config';
import { DialogRef } from '../dialog-ref';
import { FormGroup, FormControl, Validators } from '@angular/forms';

class VibratorProperties {
  power: number = null;
  time: number = null;
}

@Component({
    selector: 'DialogHwVibrator',
    templateUrl: './hwVibrator.component.html',
    styleUrls: ['./hwVibrator.component.scss']
})
export class DialogHwVibrator {
  public mOriginal: VibratorProperties = null;
  public mModifications: Array<any> = [];

  public mFormGroup = new FormGroup({
    power: new FormControl({ value: null }, [Validators.required, Validators.min(0.1), Validators.max(1.0)]),
    time: new FormControl({ value: null }, [Validators.required, Validators.min(1), Validators.max(1000)]),
  })


  constructor(public mDialogConfig: DialogConfig, public mDialogRef: DialogRef) {
    this.mOriginal = this.mDialogConfig.data.node.properties;
    this.mFormGroup.controls.power.setValue(this.mOriginal.power);
    this.mFormGroup.controls.time.setValue(this.mOriginal.time);
    //markAsPristine();
  }


  public save(): void {
    if (this.mFormGroup.controls.power.dirty && (this.mFormGroup.controls.power.value !== this.mOriginal.power)) {
      this.mModifications.push({
        property: ['property', 'power'],
        value: this.mFormGroup.controls.power.value
      });
    }

    if (this.mFormGroup.controls.time.dirty && (this.mFormGroup.controls.time.value !== this.mOriginal.time)) {
      this.mModifications.push({
        property: ['property', 'time'],
        value: this.mFormGroup.controls.time.value
      });
    }

    this.mDialogRef.close(true, this.mModifications)
  }


  public reset(): void {
    this.mFormGroup.reset(this.mOriginal);
  }


  public cancel(): void {
    this.mDialogRef.close(false);
  }
}