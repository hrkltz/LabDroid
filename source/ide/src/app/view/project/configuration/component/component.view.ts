import { Component, OnInit, OnDestroy, HostListener } from '@angular/core';
import { WebSocketService } from '../../../../service/web-socket.service';
import { Router, ActivatedRoute, NavigationCancel } from '@angular/router';
import { FormGroup, FormControl, Validators } from '@angular/forms';
import { first, filter } from 'rxjs/operators';
import { deepCopy } from 'src/app/helper';
import { Subscription, Observable } from 'rxjs';
import { InfoBoxService } from 'src/app/service/infobox.service';
import { ToastService } from 'src/app/toast';
import { DialogBoolean } from 'src/app/dialog/generic/boolean/boolean.component';
import { DialogService } from 'src/app/dialog/dialog.service';
import { Location } from '@angular/common';



@Component({
  selector: 'chmlnProjectConfigurationComponentView',
  templateUrl: './component.view.html',
  styleUrls: ['./component.view.scss']
})
export class ConfigurationComponentView implements OnInit, OnDestroy {
  private mSubscriptions: Array<Subscription> = new Array()
  private mProjectName: string
  public mComponentId: string
  public mFormGroup: FormGroup = new FormGroup({})
  public mEmpty: boolean = true
  public mBlueprints: any = null


  @HostListener('document:keydown', ['$event'])
  handleKeyboardEvent(event: KeyboardEvent) {
    if (event.ctrlKey) {
      switch (event.key) {
        case 's':
          this.save()
			    return( false )
        case 'd':
          this.delete()
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
    private mActivatedRoute: ActivatedRoute,
    private mRouter: Router,
    private mLocation: Location,
    private mWebSocketService: WebSocketService,
    private mDialogService: DialogService,
    private mInfoBoxService: InfoBoxService,
    private mToastService: ToastService) {
      this.mProjectName = this.mActivatedRoute.snapshot.paramMap.get('projectName')
  }


  ngOnInit() {
    this.mInfoBoxService.mText = 'configurationComponent'

    // We need to subscribe because we use the same component for all components.
    // Angular doesnt reload if we switch fro mcomponent to component
    this.mSubscriptions.push(this.mActivatedRoute.params.subscribe(params => {
      this.mComponentId = params.componentId
      
      this.mBlueprints = null
      this.mFormGroup = new FormGroup({})

      const lConfiguration = this.mWebSocketService.mProject.configurations.get(this.mComponentId)

      if (lConfiguration.blueprints == undefined)
        return

      this.mBlueprints = deepCopy(lConfiguration.blueprints)

      for (let key in this.mBlueprints) {
        this.mFormGroup.addControl(key, new FormControl(lConfiguration[key]))

        switch (this.mBlueprints[key].type) {
          case "Integer":
          case "Long":
          case "Float":
          case "Double":
            this.mFormGroup.controls[key].setValidators([Validators.min(this.mBlueprints[key].min), Validators.max(this.mBlueprints[key].max)])
          break;
          case "String":
            this.mFormGroup.controls[key].setValidators([Validators.pattern(this.mBlueprints[key].regex)])
            break;
        }
      }

      this.mEmpty = (Object.keys(this.mFormGroup.controls).length == 0)
    }))
    
    // This is a workaround to solve https://github.com/angular/angular/issues/16314
    this.mRouter.events.pipe(filter((event) => event instanceof NavigationCancel)).subscribe(
      () => this.mLocation.replaceState('')
    )
  }


  ngOnDestroy() {
    this.mSubscriptions.forEach(value => value.unsubscribe())
    this.mInfoBoxService.mText = 'empty'
  }


  public save(): void {
    if (!this.mFormGroup.valid) {
      this.mToastService.show({
        type: 'warning',
        text: 'Invalid configuration'
      })
      
      return
    }

    let configuration = {}

    Object.keys(this.mFormGroup.controls).forEach((key) => {
      configuration[key] = this.mFormGroup.controls[key].value
    })

    this.mWebSocketService.Component.save$.pipe(first()).subscribe((pResult) => {
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

    this.mWebSocketService.Component.save(this.mProjectName, this.mComponentId, configuration)
  }

  /* NTH
  public reset(): void {
    
    // 1. Time: Revert to current configuration
    // 2. Time: If it is current configuration reset to initial/default configuration
  }*/


  public delete(): void {
    this.mDialogService.open(DialogBoolean, {
      data: {
        header: 'Delete Component',
        main: 'Are you sure?'
      }
    }).afterClosedSimple.pipe(first()).subscribe((ppResult) => {
      if (ppResult) {
        this.mWebSocketService.Component.delete$.pipe(first()).subscribe((result) => {
          if (result)
            this.mRouter.navigate([this.mProjectName, 'config'], { skipLocationChange: true })
        })
    
        this.mWebSocketService.Component.delete(this.mProjectName, this.mComponentId)
      }
    })
  }
}