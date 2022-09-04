import { NgModule, ModuleWithProviders } from '@angular/core'
import { OverlayModule } from '@angular/cdk/overlay';

import { ToastComponent } from './toast.component';
import { defaultToastConfig, TOAST_CONFIG_TOKEN } from './toast-config';
import { CommonModule } from '@angular/common';

@NgModule({
  imports: [OverlayModule, 
    CommonModule],
  declarations: [ToastComponent],
  entryComponents: [ToastComponent]
})
export class ToastModule {
  public static forRoot(config = defaultToastConfig): ModuleWithProviders<ToastModule> {
        return {
            ngModule: ToastModule,
            providers: [
                {
                    provide: TOAST_CONFIG_TOKEN,
                    useValue: config // bug if compiled for production: { ...defaultToastConfig, ...config }, see https://medium.com/@ezzabuzaid/this-will-not-work-in-production-because-of-the-global-configuration-token-50634991833b
                },
            ],
        };
    }
 }