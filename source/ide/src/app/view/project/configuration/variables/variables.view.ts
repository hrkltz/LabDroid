import { Component, HostListener, KeyValueDiffer, KeyValueDiffers } from '@angular/core'
import { ActivatedRoute, Router, NavigationCancel } from '@angular/router'
import { FormGroup, FormControl, Validators, ValidatorFn, AbstractControl } from '@angular/forms'

import { InfoBoxService } from 'src/app/service/infobox.service'
import { WebSocketService } from '../../../../service/web-socket.service'
import { first, filter } from 'rxjs/operators'
import { ToastService } from 'src/app/toast'
import { Observable } from 'rxjs'
import { Location } from '@angular/common'
import { Utils } from 'src/app/utils'


@Component({
  selector: 'chmlnProjectConfigurationVariablesView',
  templateUrl: './variables.view.html',
  styleUrls: ['./variables.view.scss']
})
export class ConfigurationVariablesView {
  private mProjectName: string
  public mFormGroup: FormGroup = new FormGroup({})


  @HostListener('document:keydown', ['$event'])
  handleKeyboardEvent(event: KeyboardEvent) {
    if (event.ctrlKey) {
      switch (event.key) {
        case 's':
          this.save()
			    return( false )
      }
    }
  }


  // @HostListener allows us to also guard against browser refresh, close, etc.
  @HostListener('window:beforeunload')
  canDeactivate(): Observable<boolean> | boolean {
    return !this.mFormGroup.dirty
  }


  constructor(
    pActivatedRoute: ActivatedRoute,
    private mRouter: Router,
    private mLocation: Location,
    private mInfoBoxService: InfoBoxService,
    private mToastService: ToastService,
    public mWebSocketService: WebSocketService) {
      this.mProjectName = pActivatedRoute.snapshot.paramMap.get('projectName')

      for (const [key, value] of Object.entries(this.mWebSocketService.mProject.variables)) {
        this.mFormGroup.addControl(key, new FormGroup({
          'label': new FormControl(value.label, [
            Validators.required, 
            Validators.maxLength(25),
            Validators.pattern('^[a-zA-Z][a-zA-Z0-9_]*$'),
            this.notExists()]),
          'datatype': new FormControl(value.datatype, [Validators.required]),
          'value': new FormControl(value.value)
        }))

        this.mFormGroup.get(key).get('datatype').valueChanges.subscribe(
          () => {
            this.mFormGroup.get(key).get('value').setValue(null)
            this.mFormGroup.get(key).get('value').markAsDirty()
        })
      }
  }


  ngOnInit() {
    this.mInfoBoxService.mText = 'configurationSettings'
    
    // This is a workaround to solve https://github.com/angular/angular/issues/16314
    this.mRouter.events.pipe(filter((event) => event instanceof NavigationCancel)).subscribe(
      () => this.mLocation.replaceState('')
    )
  }


  ngOnDestroy() {
    this.mInfoBoxService.mText = 'empty'
  }


  public save(): void {
    if (!this.mFormGroup.valid) {
      this.mToastService.show({
        type: 'warning',
        text: 'Invalid value(s)!'
      })

      return
    }
    
    let lVariables = { }

    for (let control in this.mFormGroup.controls) {
      const lSubFormGroup = this.mFormGroup.controls[control] as FormGroup

      lVariables[control] = {
        'label': lSubFormGroup.controls['label'].value,
        'datatype': lSubFormGroup.controls['datatype'].value,
        'value': lSubFormGroup.controls['value'].value
      }

      if (lVariables[control].datatype == 'number' && Number(lVariables[control].value))
        lVariables[control].value = Number(lVariables[control].value)
    }

    const lSettings = {
      'variables': lVariables
    }

    this.mWebSocketService.Project2.save$.pipe(first()).subscribe((pResult) => {
      if (pResult) {
        (document.activeElement as any).blur()
        this.mFormGroup.markAsPristine()

        this.mToastService.show({
          type: 'success',
          text: 'Saved'
        })
      } else
        this.mToastService.show({
          type: 'error',
          text: 'Couldn\'t save'
        })
    })

    this.mWebSocketService.Project2.save(this.mProjectName, lSettings)
  }


  private notExists(): ValidatorFn {
    return (pAbastractControl: AbstractControl): { [key: string]: any } | null => {
      for (let control in this.mFormGroup.controls) {
        const lSubFormGroup = this.mFormGroup.controls[control] as FormGroup

        // Ignore the own control.
        if (pAbastractControl.parent == lSubFormGroup)
          continue

        // If another control use the same label throw an error.
        if (lSubFormGroup.controls['label'].value == pAbastractControl.value)
          return pAbastractControl.value
      }
      
      return null
    }
  }


  public add(): void {
    const id = Utils.randomString(8)

    this.mFormGroup.addControl(id, new FormGroup({
      'label': new FormControl(null, [
        Validators.required, 
        Validators.maxLength(25),
        Validators.pattern('^[a-zA-Z][a-zA-Z0-9_]*$'),
        this.notExists()]),
      'datatype': new FormControl('number', [Validators.required]),
      'value': new FormControl(null)
    }))

    this.mFormGroup.get(id).get('datatype').valueChanges.subscribe(
      () => {
        this.mFormGroup.get(id).get('value').setValue(null)
        this.mFormGroup.get(id).get('value').markAsDirty()
    })
  }
}