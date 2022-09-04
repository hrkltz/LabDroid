import { Component, AfterViewInit, ViewChild, ElementRef } from '@angular/core'
import { DialogConfig } from '../../dialog-config'
import { DialogRef } from '../../dialog-ref'
import { FormGroup, FormControl, Validators, ValidatorFn, AbstractControl } from '@angular/forms'



@Component({
  selector: 'dialogCreateScript',
  templateUrl: './create_script.component.html',
  styleUrls: ['./create_script.component.scss']
})
export class DialogCreateScript implements AfterViewInit {
  @ViewChild('name', { static: true }) mName: ElementRef

  public mFormGroup = new FormGroup({
    name: new FormControl(null, [
      Validators.maxLength(25),
      Validators.pattern('^[a-zA-Z][a-zA-Z0-9_]*$')]),
    inputCount: new FormControl('1', [Validators.required, this.notZero()]),
    outputCount: new FormControl('1', [Validators.required, this.notZero()])
  })


  constructor(
    public mDialogConfig: DialogConfig,
    public mDialogRef: DialogRef) { }


  ngAfterViewInit() {
    window.setTimeout(() => {
      // Defer this code to another Javascript Virtual Machine turn.
      // Source: https://blog.angular-university.io/angular-debugging/
      this.mName.nativeElement.focus()
    })
  }


  private notZero(): ValidatorFn {
    return (pAbastractControl: AbstractControl): { [key: string]: any } | null => {
      if (pAbastractControl.parent) {
        const lCount: number = Number.parseInt(pAbastractControl.parent
          .controls['inputCount'].value) + Number.parseInt(pAbastractControl
            .parent.controls['outputCount'].value)
            
        if (lCount === 0) {
          pAbastractControl.parent.controls['inputCount'].setErrors({})
          pAbastractControl.parent.controls['outputCount'].setErrors({})

          return pAbastractControl.value
        } else {
          pAbastractControl.parent.controls['inputCount'].setErrors(null)
          pAbastractControl.parent.controls['outputCount'].setErrors(null)

          return null
        }
      }

      return null
    }
  }


  public save(): void {
    this.mDialogRef.close(true, {
      name: this.mFormGroup.controls.name.value ? this.mFormGroup.controls.name.value : '',
      inputCount: Number.parseInt(this.mFormGroup.controls.inputCount.value, 10),
      outputCount: Number.parseInt(this.mFormGroup.controls.outputCount.value,
        10)
    })
  }
}
