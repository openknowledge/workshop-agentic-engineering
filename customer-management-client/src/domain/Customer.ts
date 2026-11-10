import { z } from 'zod';

export const customerSchema = z.object({
  name: z.object({
    firstName: z
      .string()
      .min(1, 'First name is required')
      .max(255, 'First name must be at most 255 characters long')
      .regex(/^\D*$/, 'First name must not contain digits'),
    lastName: z
      .string()
      .min(1, 'Last name is required')
      .max(255, 'Last name must be at most 255 characters long')
      .regex(/^\D*$/, 'Last name must not contain digits'),
  }),
  email: z
    .string()
    .min(1, 'Email is required')
    .email('Invalid email address')
    .max(255, 'Email must be at most 255 characters long'),
  phoneNumber: z
    .string()
    .max(50, 'Phone number must be at most 50 characters long')
    .regex(
      /^\+?[0-9 ()/-]*$/,
      'Phone number may only contain digits, spaces and the characters +()/-'
    )
    .or(z.undefined()),
  birthDate: z.string().or(z.undefined()),
});

export type Customer = z.infer<typeof customerSchema>;
