"use strict";(self.webpackChunkvalidator_frontend=self.webpackChunkvalidator_frontend||[]).push([[962,118],{6275:function(e,t,n){n.d(t,{Z:function(){return o}});var a=n(7294),r=n(5833),i=n(7547),l=n(7506);var o=function(e){let{endpoint:t,fileName:n}=e;const{request:o,data:c,error:u,status:d}=(0,l.Z)();return(0,a.useEffect)((()=>{o(t,{headers:{"Content-Type":"application/xml"}})}),[t,o]),a.createElement(a.Fragment,null,d===l.e.Failure&&u&&a.createElement(i.Z,{title:"An error occurred while fetching"},a.createElement(r.Z,null,u.message)),d===l.e.Success&&c&&a.createElement(r.Z,{download:{fileName:n,mime:"application/xml"},enableCopy:!0},c))}},3596:function(e,t,n){n.r(t);var a=n(7294),r=n(8222),i=n(6275);t.default=function(){return a.createElement(r.Z,{title:"Validator configuration",layoutDescription:"The currently loaded validator configuration",headline:"Validator configuration",description:"View the currently loaded validator configuration."},a.createElement(i.Z,{endpoint:"/server/config",fileName:"config.xml"}))}},2536:function(e,t,n){n.r(t);var a=n(3596);t.default=a.default}}]);