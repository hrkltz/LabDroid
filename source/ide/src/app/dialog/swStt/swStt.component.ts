import { Component } from '@angular/core';
import { DialogConfig } from '../dialog-config';
import { DialogRef } from '../dialog-ref';
import { FormGroup, FormControl } from '@angular/forms';


class Properties {
  language: String = null;
}


@Component({
    selector: 'DialogSwStt',
    templateUrl: './swStt.component.html',
    styleUrls: ['./swStt.component.scss']
})
export class DialogSwStt {
  public mOriginal: Properties = null;
  public mModifications: Array<any> = [];
  public mFormGroup = new FormGroup({
    language: new FormControl(),
  })


  constructor(public mDialogConfig: DialogConfig, public mDialogRef: DialogRef) {
    this.mOriginal = this.mDialogConfig.data.node.properties;
    this.mFormGroup.controls.language.setValue(this.mOriginal.language);
  }

  
  public save(): void {
    if (this.mFormGroup.controls.language.dirty && (this.mFormGroup.controls.language.value !== this.mOriginal.language)) {
      this.mModifications.push({
        property: ['property', 'language'],
        value: this.mFormGroup.controls.language.value
      });
    }

    this.mDialogRef.close(true, this.mModifications);
  }


  public reset(): void {
    this.mFormGroup.reset(this.mOriginal);
  }


  public cancel(): void {
    this.mDialogRef.close(false, null);
  }
}