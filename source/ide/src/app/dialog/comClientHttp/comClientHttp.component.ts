import { Component } from '@angular/core';
import { DialogConfig } from '../dialog-config';
import { DialogRef } from '../dialog-ref';
import { FormGroup, FormControl, Validators } from '@angular/forms';
import { Subject, Subscription } from 'rxjs';

class Properties {
  body: String = null;
  methode: String = null;
  url: String = null;
}

@Component({
    selector: 'DialogComClientHttp',
    templateUrl: './comClientHttp.component.html',
    styleUrls: ['./comClientHttp.component.scss']
})
export class DialogComClientHttp {
  private methode: Subscription;

  public mOriginal: Properties = null;
  public mModifications: Array<any> = [];
  public mFormGroup = new FormGroup({
    body: new FormControl(),
    methode: new FormControl(),
    url: new FormControl(),
  })


  constructor(public mDialogConfig: DialogConfig, public mDialogRef: DialogRef) {
    this.mOriginal = this.mDialogConfig.data.node.properties;
    this.mFormGroup.controls.methode.setValue(this.mOriginal.methode);
    this.mFormGroup.controls.url.setValue(this.mOriginal.url);
    this.mFormGroup.controls.body.setValue(this.mOriginal.body);
  }


  ngOnInit(): void {
    this.methode = this.mFormGroup.controls.methode.valueChanges.subscribe(
      (pMethode: string) => {
        if (pMethode == 'POST' || pMethode == 'DELETE') {
          this.mFormGroup.controls.body.enable();
        } else {
          this.mFormGroup.controls.body.disable();
          this.mFormGroup.controls.body.setValue(null);
          this.mFormGroup.controls.body.markAsDirty();
        }
      });
  }


  ngOnDestroy(): void {
    this.methode.unsubscribe();
  }

  
  public save(): void {
    if (this.mFormGroup.controls.url.dirty && (this.mFormGroup.controls.url.value !== this.mOriginal.url)) {
      this.mModifications.push({
        property: ['property', 'url'],
        value: this.mFormGroup.controls.url.value
      });
    }

    if (this.mFormGroup.controls.methode.dirty && (this.mFormGroup.controls.methode.value !== this.mOriginal.methode)) {
      this.mModifications.push({
        property: ['property', 'methode'],
        value: this.mFormGroup.controls.methode.value ? this.mFormGroup.controls.methode.value : null
      });
    }

    if (this.mFormGroup.controls.body.dirty && (this.mFormGroup.controls.body.value !== this.mOriginal.body)) {
      this.mModifications.push({
        property: ['property', 'body'],
        value: this.mFormGroup.controls.body.value
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