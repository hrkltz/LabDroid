import { Injectable, Injector, Inject } from '@angular/core';
import { Overlay, GlobalPositionStrategy } from '@angular/cdk/overlay';
import { ComponentPortal, PortalInjector } from '@angular/cdk/portal';

import { ToastComponent } from './toast.component';
import { ToastData, TOAST_CONFIG_TOKEN, ToastConfig, ToastHorizontalPosition, ToastVerticalPosition } from './toast-config';
import { ToastRef } from './toast-ref';
import { ToastModule } from './toast.module';

@Injectable({
  providedIn: ToastModule
})
export class ToastService {
    private lastToast: ToastRef;


    constructor(
        private overlay: Overlay,
        private parentInjector: Injector,
        @Inject(TOAST_CONFIG_TOKEN) public toastConfig: ToastConfig
    ) { }


    public show(data: ToastData) {
        const positionStrategy = this.getPositionStrategy();
        const overlayRef = this.overlay.create({ positionStrategy });

        const toastRef = new ToastRef(overlayRef);
        this.lastToast = toastRef;

        const injector = this.getInjector(data, toastRef, this.parentInjector);
        const toastPortal = new ComponentPortal(ToastComponent, null, injector);

        overlayRef.attach(toastPortal);

        return toastRef;
    }


    public getPositionStrategy(): GlobalPositionStrategy {
        let lPositionStrategy = this.overlay.position().global();

        switch (this.toastConfig.position.horizontal) {
            case ToastHorizontalPosition.left:
                lPositionStrategy.left(`${this.toastConfig.position.margin}px`);
            break;
            case ToastHorizontalPosition.center:
                lPositionStrategy.centerHorizontally();
            break;
            case ToastHorizontalPosition.right:
                lPositionStrategy.right(`${this.toastConfig.position.margin}px`);
            break;
        }

        const lPosition: string = `${this.getPosition()}px`;

        switch (this.toastConfig.position.vertical) {
            case ToastVerticalPosition.top:
                lPositionStrategy.top(lPosition);
            break;
            case ToastVerticalPosition.bottom:
                lPositionStrategy.bottom(lPosition);
            break;
        }

        return lPositionStrategy;
    }


    public getPosition(): number {
        let lPosition: number = this.toastConfig.position.margin;
        const lastToastIsVisible: HTMLElement = this.lastToast && this.lastToast.isVisible();

        if (lastToastIsVisible) {
            switch (this.toastConfig.position.vertical) {
                case ToastVerticalPosition.top:
                    lPosition = this.lastToast.getPosition().bottom
                break;
                case ToastVerticalPosition.bottom:
                    lPosition = window.innerHeight - this.lastToast.getPosition().top
                break;
            } 
        }

        return lPosition;
    }


    public getInjector(data: ToastData, toastRef: ToastRef, parentInjector: Injector) {
        const tokens = new WeakMap();

        tokens.set(ToastData, data);
        tokens.set(ToastRef, toastRef);

        return new PortalInjector(parentInjector, tokens);
    }
}