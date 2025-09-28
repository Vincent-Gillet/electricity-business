import {HttpContext, HttpContextToken, HttpInterceptorFn} from '@angular/common/http';

// Défini les routes publiques avec un context
export const IS_PUBLIC = new HttpContextToken<boolean>(() => true);

// Intercepteur HTTP pour ajouter le token d'authentification
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  console.log("AuthInterceptor : ", req.url);
  console.log("Body de la réponse : ", req.body);

  const token = localStorage.getItem('token');

  //Vérifier
  if (req.context.get(IS_PUBLIC) == false) {
    const authReq = req.clone({
      setHeaders: {
        "Authorization": "Bearer " + token,
      }
    })
    console.log(authReq);

    return next(authReq);
  }

  console.log(req);
  return next(req);
};
