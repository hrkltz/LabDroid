import { Component, HostListener } from '@angular/core'
import { ActivatedRoute, Router, NavigationCancel } from '@angular/router'
import { FormGroup, FormControl } from '@angular/forms'

import { InfoBoxService } from 'src/app/service/infobox.service'
import { WebSocketService } from '../../../../service/web-socket.service'
import { first, filter } from 'rxjs/operators'
import { ToastService } from 'src/app/toast'
import { Observable } from 'rxjs'
import { Location } from '@angular/common'


@Component({
  selector: 'chmlnProjectConfigurationSettingsView',
  templateUrl: './settings.view.html',
  styleUrls: ['./settings.view.scss']
})
export class ConfigurationSettingsView {
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

      this.mFormGroup.addControl('screen', new FormControl(this.mWebSocketService.mProject.screen))
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
    if (!this.mFormGroup.valid)
      return
    
    let lSettings = {}

    Object.keys(this.mFormGroup.controls).forEach((key) => {
      lSettings[key] = this.mFormGroup.controls[key].value
    })

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
}