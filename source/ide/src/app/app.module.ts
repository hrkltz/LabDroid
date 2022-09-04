import { NgModule, LOCALE_ID } from '@angular/core'
import { BrowserModule, Title } from '@angular/platform-browser'
import { registerLocaleData } from '@angular/common'
import { TranslateModule, TranslateService, TranslateLoader } from '@ngx-translate/core' 
import { TranslateHttpLoader } from '@ngx-translate/http-loader'
import localeDe from '@angular/common/locales/de'
import { HttpClient, HttpClientModule } from '@angular/common/http'
import localeDeExtra from '@angular/common/locales/extra/de'
import { FormsModule, ReactiveFormsModule } from '@angular/forms'
import { RouterModule, Routes } from '@angular/router'
import { BrowserAnimationsModule } from '@angular/platform-browser/animations'
import {  MatAutocompleteModule } from '@angular/material/autocomplete'
import {  MatButtonModule } from '@angular/material/button'
import {  MatButtonToggleModule } from '@angular/material/button-toggle'
import {  MatCardModule } from '@angular/material/card'
import {  MatCheckboxModule } from '@angular/material/checkbox'
import {  MatChipsModule } from '@angular/material/chips'
import {  MatDialogModule } from '@angular/material/dialog'
import {  MatExpansionModule } from '@angular/material/expansion'
import {  MatFormFieldModule } from '@angular/material/form-field'
import {  MatGridListModule } from '@angular/material/grid-list'
import {  MatIconModule } from '@angular/material/icon'
import {  MatInputModule } from '@angular/material/input'
import {  MatListModule } from '@angular/material/list'
import {  MatMenuModule } from '@angular/material/menu'
import {  MatProgressBarModule } from '@angular/material/progress-bar'
import {  MatProgressSpinnerModule } from '@angular/material/progress-spinner'
import {  MatSelectModule } from '@angular/material/select'



import { AppComponent } from './app.component'
import { DialogModule } from './dialog/dialog.module'
import { DialogBoolean } from './dialog/generic/boolean/boolean.component'
import { DialogInput } from './dialog/generic/input/input.component'
import { DialogAdd } from './dialog/add/add.component'
import { DialogSwScript } from './dialog/swScript/swScript.component'
import { IndexComponent } from './view/index/index.view'
import { DialogHwVibrator } from './dialog/hwVibrator/hwVibrator.component'
import { DialogComClientHttp } from './dialog/comClientHttp/comClientHttp.component'
import { DialogCreateScript } from './dialog/create/create_script/create_script.component'
import { DialogSwStt } from './dialog/swStt/swStt.component'
import { IconComponent } from './icon/icon.component'
import { DialogNewProject } from './dialog/new_project/new_project.dialog'
import { DialogChannel } from './dialog/channel/channel.dialog'
import { ProjectComponent } from './view/project/project.view'
import { BackendView } from './view/project/backend/backend.view'
import { ConfigurationView } from './view/project/configuration/configuration.view'
import { DialogSelect } from './dialog/generic/select/select.component'
import { ConfigurationComponentView } from './view/project/configuration/component/component.view'
import { ConfigurationFileView } from './view/project/configuration/file/file.view'
import { AppGuard } from './app.guard'
import { ConfigurationSettingsView } from './view/project/configuration/settings/settings.view'
import { ConfigurationVariablesView } from './view/project/configuration/variables/variables.view'
import { FrontendView } from './view/project/frontend/frontend.view'
import { LogView } from './view/project/log/log.view'
import { ToastModule } from './toast'
import { DialogUnsaved } from './dialog/unsaved/unsaved.component'
import { OrderByPipe } from './pipes/orderBy.pipe'
import { OrderPipe } from './pipes/orderBy2.pipe'


registerLocaleData(localeDe, 'de-DE', localeDeExtra)


export function createTranslateLoader(pHttpClient: HttpClient) {
  return new TranslateHttpLoader(pHttpClient, 'assets/i18n/', '.json' )
}

const appRoutes: Routes = [
  {
    path: '',
    component: IndexComponent,
    pathMatch: 'full',
    canActivate: [AppGuard]
  },
  {
    path: ':projectName',
    component: ProjectComponent,
    canActivate: [AppGuard],
    children: [
      {
        path: 'backend',
        component: BackendView
      },
      {
        path: 'config',
        component: ConfigurationView,
        children: [
          { component: ConfigurationSettingsView, path: '', canDeactivate: [AppGuard] },
          { component: ConfigurationVariablesView, path: 'variables', canDeactivate: [AppGuard] },
          { component: ConfigurationFileView, path: 'files' },
          { component: ConfigurationComponentView, path: ':componentId', canDeactivate: [AppGuard] }
        ]
      },
      {
        path: 'frontend',
        component: FrontendView,
        canDeactivate: [AppGuard]
      },
      {
        path: 'log',
        component: LogView
      },
      {
        path: '**',
        redirectTo: 'backend'
      }
    ]
  },
  {
    path: '**',
    redirectTo: ''
  }
]

@NgModule({
  declarations: [
    AppComponent,
    DialogAdd,
    DialogBoolean,
    DialogCreateScript,
    DialogComClientHttp,
    DialogInput,
    DialogSelect,
    DialogChannel,
    DialogSwScript,
    DialogHwVibrator,
    DialogNewProject,
    DialogSwStt,
    DialogUnsaved,
    IconComponent,
    IndexComponent,
    ProjectComponent,
    BackendView,
    FrontendView,
    LogView,
    OrderByPipe,
    OrderPipe,
    ConfigurationView,
    ConfigurationSettingsView,
    ConfigurationFileView,
    ConfigurationComponentView,
    ConfigurationVariablesView
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    FormsModule,
    MatAutocompleteModule,
    MatButtonModule,
    MatButtonToggleModule,
    MatCardModule,
    MatCheckboxModule,
    MatChipsModule,
    MatDialogModule,
    MatExpansionModule,
    MatFormFieldModule,
    MatGridListModule,
    MatIconModule,
    MatInputModule,
    MatListModule,
    MatMenuModule,
    MatProgressBarModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    HttpClientModule,

    DialogModule,
    ReactiveFormsModule,
    RouterModule.forRoot(appRoutes, {
      paramsInheritanceStrategy: 'always'
    }),
    ToastModule.forRoot(),
    TranslateModule.forRoot({
        loader: {
            provide: TranslateLoader,
            useFactory: ( createTranslateLoader ),
            deps: [ HttpClient ]
        }})
  ],
  bootstrap: [AppComponent],
  entryComponents: [
    DialogAdd,
    DialogBoolean,
    DialogCreateScript,
    DialogInput,
    DialogSelect,
    DialogChannel,
    DialogComClientHttp,
    DialogNewProject,
    DialogSwScript,
    DialogSwStt,
    DialogUnsaved,
    DialogHwVibrator
  ], 
  providers: [
      { provide: LOCALE_ID, useValue: 'en' },
      TranslateService,
      Title,
      Location
  ]})
export class AppModule { }
